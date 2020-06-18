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
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


public final class FindMeetingQuery {

  public static final int MINUTES_IN_DAY = 1440;

  /**
  * Returns possible meeting times on a certain day given a meeting request.
  * Handles requests with only mandatory attendees, only optional attendees, both, and neither.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> ret = new ArrayList<TimeRange>();
    long duration = request.getDuration();

    // no options for too long of a request
    if (duration > MINUTES_IN_DAY){
      return ret;
    }

    Set<String> mandatoryAttendees =  new HashSet<String>();
    mandatoryAttendees.addAll(request.getAttendees()); 

    Set<String> optionalAttendees = new HashSet<String>();
    optionalAttendees.addAll(request.getOptionalAttendees());

    Set<String> allAttendees = new HashSet<String>();
    allAttendees.addAll(mandatoryAttendees);
    allAttendees.addAll(optionalAttendees);

    // options for no attendees
    if (allAttendees.isEmpty()) {
      ret.add(TimeRange.WHOLE_DAY);
      return ret;
    }

    // find free times
    List<TimeRange> freeTimes = new ArrayList<TimeRange>(findFreeTimes(allAttendees, events, duration));
  
    if (freeTimes.isEmpty()) {
      // return empty list if there are no mandatory attendees and no free times for optional attendees
      if (mandatoryAttendees.isEmpty()) {
        return new ArrayList<TimeRange>();
      }
      // ignore optional attendees if there are no free times across both mandatory and optional attendees
      freeTimes = new ArrayList<TimeRange>(findFreeTimes(mandatoryAttendees, events, duration));
    }

    return freeTimes;
  }

  private List<TimeRange> findFreeTimes(Set<String> attendees, Collection<Event> events, long duration) {
    List<TimeRange> freeTimes = new ArrayList<TimeRange>();
    List<TimeRange> blockedTimes = new ArrayList<TimeRange>();

    // find all blocked times based on double-booked attendees
    for (Event event : events) {
      if(event.getAttendees().stream().anyMatch(attendees::contains)) {
        blockedTimes.add(event.getWhen());
      }
    }

    int lastBlockedTimeEnd = TimeRange.START_OF_DAY;
    Collections.sort(blockedTimes, TimeRange.ORDER_BY_START);

    // find all time ranges between blocked times that contain request duration time
    for (TimeRange blockedTime : blockedTimes) {
      if (blockedTime.end() > lastBlockedTimeEnd) {
        if (blockedTime.start() - lastBlockedTimeEnd >= duration) {
          freeTimes.add(TimeRange.fromStartEnd(lastBlockedTimeEnd, blockedTime.start(), false));
        }
        lastBlockedTimeEnd = blockedTime.end(); 
      }
    }

    // request duration fits within available time at end
    if (TimeRange.END_OF_DAY - lastBlockedTimeEnd >= duration) {
      freeTimes.add(TimeRange.fromStartEnd(lastBlockedTimeEnd, TimeRange.END_OF_DAY, true));
    }

    return freeTimes;
  }
}
