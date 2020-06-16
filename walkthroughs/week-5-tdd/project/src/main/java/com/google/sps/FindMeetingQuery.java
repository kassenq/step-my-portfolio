// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;
// import com.google.sps.servlets.TimeRange;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> ret = new ArrayList<TimeRange>();

    Set<String> mandatoryAttendees =  new HashSet<String>();
    mandatoryAttendees.addAll(request.getAttendees()); 

    Set<String> optionalAttendees = new HashSet<String>();
    optionalAttendees.addAll(request.getOptionalAttendees());

    Set<String> allAttendees = new HashSet<String>();
    allAttendees.addAll(mandatoryAttendees);
    allAttendees.addAll(optionalAttendees);

    long duration = request.getDuration();

    // no options for too long of a request
    if (duration > 24*60){
      return ret;
    }

    // options for no attendees
    if (allAttendees.size() == 0) {
      ret.add(TimeRange.WHOLE_DAY);
      return ret;
    }

    // find free times
    List<TimeRange> freeTimes = new ArrayList<TimeRange>(findFreeTimes(allAttendees, events, request));
    if (freeTimes.isEmpty()) {
      // ignore optional attendees if there are no free times across both mandatory and optional attendees
      freeTimes = new ArrayList<TimeRange>(findFreeTimes(mandatoryAttendees, events, request));
    } else {
      return freeTimes;
    }

    // adhere to additional test requirement
    if (mandatoryAttendees.isEmpty()) {
      return new ArrayList<TimeRange>();
    }
    return freeTimes;
  }

  private List<TimeRange> mergeOverlaps(List<TimeRange> blocked) {
    // find if blocked times have overlap and consolidate them into sorted arraylist
    Collections.sort(blocked, TimeRange.ORDER_BY_START);
    List<TimeRange> merged = new ArrayList<TimeRange>();
    merged.add(blocked.get(0));
    int idx = 0;
    for (int i = 0; i < blocked.size(); i++) {
      if (blocked.get(i).overlaps(merged.get(idx))) {
        merged.set(idx, TimeRange.fromStartEnd(
          Math.min(merged.get(idx).start(), blocked.get(i).start()),
          Math.max(merged.get(idx).end(), blocked.get(i).end()) - 1, 
          true));
      } else {
        idx += 1;
        merged.add(blocked.get(i));
      }
    }
    return merged;
  }

  private List<TimeRange> findFreeTimes(Set<String> attendees, Collection<Event> events, MeetingRequest request) {
    // loop through blocked times, add free time ranges to ret
    List<TimeRange> freeTimes = new ArrayList<TimeRange>();
    List<TimeRange> blockedTimes = new ArrayList<TimeRange>();
    long duration = request.getDuration();

    for (Event event : events) {
      Set<String> eventAttendees = event.getAttendees();
      Set<String> overlappingAttendees = new HashSet<>(eventAttendees);
      overlappingAttendees.retainAll(attendees);
      if (!overlappingAttendees.isEmpty()) {
        blockedTimes.add(event.getWhen());
      }
    }

    int start = TimeRange.START_OF_DAY;

    // find all time ranges between blocked times that contain request duration time
    for (TimeRange blockedTime : blockedTimes) {
      if (blockedTime.end() > start) {
        if (blockedTime.start() - start >= duration) {
          freeTimes.add(TimeRange.fromStartEnd(start, blockedTime.start(), false));
        }
        start = blockedTime.end(); 
      }
    }

    // request duration fits within available time at end
    if (TimeRange.END_OF_DAY - start >= duration) {
      freeTimes.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }

    // double check for overlap
    for (int i = 0; i < freeTimes.size(); i++) {
      for (TimeRange blockedTime: blockedTimes) {
        if (freeTimes.get(i).overlaps(blockedTime)) {
          freeTimes.remove(freeTimes.get(i));
        }
      }
    }
    return freeTimes;
  }
}
