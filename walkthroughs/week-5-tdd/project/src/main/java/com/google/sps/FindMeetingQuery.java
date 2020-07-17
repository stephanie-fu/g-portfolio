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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Base cases
    if (request.getAttendees().isEmpty()) { 
      return Arrays.asList(TimeRange.WHOLE_DAY); 
    }

    if (request.getDuration() >= TimeRange.WHOLE_DAY.duration()) { 
      return new ArrayList<>();
    }
    
    // Preprocess event input
    List<Event> pertinentEvents = events.stream()
                                        .filter(event -> eventContainsAttendees(event, new HashSet<>(request.getAttendees())) && 
                                                         eventCleared(new ArrayList<>(events), event))
                                        .sorted((e1, e2) -> TimeRange.ORDER_BY_END.compare(e1.getWhen(), e2.getWhen()))
                                        .collect(Collectors.toList());

    List<TimeRange> openSlots = new ArrayList<>();
    int previousEnd = 0;

    for (Event event : pertinentEvents) {
      TimeRange eventTimeRange = event.getWhen();
      if (eventTimeRange.start() - previousEnd >= request.getDuration()) {
        openSlots.add(TimeRange.fromStartEnd(previousEnd, eventTimeRange.start(), /* inclusive = */ false));
      }
      previousEnd = eventTimeRange.end();
    }
    // Process a potential time slot at the end of the day
    if (TimeRange.END_OF_DAY - previousEnd >= request.getDuration()) {
      openSlots.add(TimeRange.fromStartEnd(previousEnd, TimeRange.END_OF_DAY, /* inclusive = */ true));
    }
    return openSlots;
  }

  private static boolean eventContainsAttendees(Event event, Set<String> attendees) {
    Set<String> eventAttendees = new HashSet<>(event.getAttendees());
    eventAttendees.retainAll(attendees);
    return eventAttendees.size() > 0;
  }

  private static boolean eventCleared(List<Event> events, Event event) {
    boolean eventCleared = true;
    for (Event event1 : events) {
      // Invalidates event if it is contained by event1
      if (event != event1) { 
        eventCleared = !event1.getWhen().contains(event.getWhen()); 
      }
    }
    return eventCleared;
  }
}
