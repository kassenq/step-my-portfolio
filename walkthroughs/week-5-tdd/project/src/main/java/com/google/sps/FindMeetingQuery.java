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

    Set<String> attendees =  new HashSet<String>();
    attendees.addAll(request.getAttendees()); 

    Set<String> optionalAttendees = new HashSet<String>();
    optionalAttendees.addAll(request.getOptionalAttendees());

    long duration = request.getDuration();

    // no options for too long of a request
    if (duration > 24*60){
      return ret;
    }

    // options for no attendees
    if (attendees.size() == 0) {
      ret.add(TimeRange.WHOLE_DAY);
      return ret;
    }

    // double booked people (in both event and meeting request)
    List<TimeRange> blockedTimes = new ArrayList<TimeRange>();
    List<TimeRange> blockedTimesOptional = new ArrayList<TimeRange>();
    for (Event event : events) {
      Set<String> eventAttendees = event.getAttendees();
      Set<String> overlappingAttendees = new HashSet<>(eventAttendees);
      overlappingAttendees.retainAll(attendees);
      Set<String> overlappingAttendeesOptional = new HashSet<>(eventAttendees);
      overlappingAttendeesOptional.retainAll(optionalAttendees);

      if (!overlappingAttendees.isEmpty()){
        blockedTimes.add(event.getWhen());
      }
      
      if (!overlappingAttendeesOptional.isEmpty()) {
        blockedTimesOptional.add(event.getWhen());
      }
    }

    // no blocked times
    // if (blockedTimes.isEmpty()) {
    //   ret.add(TimeRange.WHOLE_DAY);
    //   return ret;
    // }
    if (blockedTimes.isEmpty() && blockedTimesOptional.isEmpty()) {
      ret.add(TimeRange.WHOLE_DAY);
      return ret;
    }
    // consolidate if necessary 
    if (!blockedTimes.isEmpty() && blockedTimesOptional.isEmpty()) {
      return findFreeTimes(mergeOverlaps(blockedTimes), duration);
    }
    if (blockedTimes.isEmpty() && !blockedTimesOptional.isEmpty()) {
      return findFreeTimes(mergeOverlaps(blockedTimesOptional), duration);
    }
    List<TimeRange> bothBlocked = new ArrayList<TimeRange>();
    bothBlocked.addAll(blockedTimes);
    bothBlocked.addAll(blockedTimesOptional);
    List<TimeRange> bothFree = findFreeTimes(mergeOverlaps(bothBlocked), duration);
    
    if (bothFree.isEmpty()) {
      // ignore optional attendees
      return findFreeTimes(mergeOverlaps(blockedTimes), duration);
    }
    return bothFree;
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

  private List<TimeRange> findFreeTimes(List<TimeRange> times, long duration) {
    // loop through blocked times, add free time ranges to ret
    List<TimeRange> ret = new ArrayList<TimeRange>();
    List<TimeRange> wholeDay = new ArrayList<>(Arrays.asList(TimeRange.WHOLE_DAY));
    int start = TimeRange.START_OF_DAY;
    for (TimeRange time : times) {
      if (time.end() > start) {
        // request duration fits within available time at start
        if (time.start() - start >= duration) {
          ret.add(TimeRange.fromStartEnd(start, time.start(), false));
        }
        // move to next available free block
        start = time.end();
      }
    }
    // request duration fits within available time at end
    if (TimeRange.END_OF_DAY - start >= duration) {
      ret.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }
    return ret;
  }
}
