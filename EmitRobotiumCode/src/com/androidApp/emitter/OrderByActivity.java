package com.androidApp.emitter;

import java.util.ArrayList;
import java.util.List;

import com.androidApp.util.Constants;

/**
 * often, events come in out of sequence, overlapped by activity.  We mark every event with the activity, and its
 * hashcode for unique identification, and we want to preserve the ordering of the events, but de-interleave them
 * by activity, so we want to change
 * a1 a2 a3 a4 a5 b1 b2 a6 a7 b3 b4
 * into
 * a1 a2 a3 a4 a5 a6 a7 b1 b2 b3 b4
 * 
 * Except that we don't want to change
 * a1 a2 a3 a4 a5 b1 b2 b3 b4 a6 a7 a8 into
 * a1 a2 a3 a4 a5 a6 a7 a8 b1 b2 b3 b4 
 */
public class OrderByActivity {
	
	// an activity name and the ordered list of events recorded during the activity
	protected class ActivityEvents {
		protected String 	mActivityName;		// name of the activity (activity@324982f)
		protected boolean	mfDone;				// start a new sequence if this is true
		List<List<String>> 	mEventLines;		// events to sequence in this activity
		
		public ActivityEvents(String activityName) {
			mActivityName = activityName;
			mEventLines = new ArrayList<List<String>>();
			mfDone = false;
		}
		
		public void addEvents(List<String> Events) {
			mEventLines.add(Events);
		}
		
		public String getActivityName() {
			return mActivityName;
		}
		
		public List<List<String>> getEventLines() {
			return mEventLines;
		}
		
		public boolean isDone() {
			return mfDone;
		}
		
		public void setDone(boolean f) {
			mfDone = f;
		}
	}
	
	public List<List<String>> orderEventsByActivity(List<List<String>> EventLines) {
		List<ActivityEvents> activityEventList = new ArrayList<ActivityEvents>();
		orderEventsByActivity(EventLines, activityEventList, 0);
		return outputOrderedEvents(activityEventList);
	}
	
	/**
	 * return the ordered, de-interleaved list of Eventized events, with the activity identifier stripped off.
	 */
	public List<List<String>> outputOrderedEvents(List<ActivityEvents>	 activityEventList) {
		List<List<String>> outputEvents = new ArrayList<List<String>>();
		for (ActivityEvents ActivityEvents : activityEventList) {
			for (List<String> EventLine : ActivityEvents.getEventLines()) {
				outputEvents.add(EventLine.subList(1,  EventLine.size()));
			}			
		}
		return outputEvents;
	}
	
	/**
	 * take the activity-intereaved list of Eventized events, ond return a list of events ordered by activity
	 */
	public void orderEventsByActivity(List<List<String>> 	 EventLines, 
									  List<ActivityEvents>	 activityEventList,
									  int					 startIndex) {
		ActivityEvents previousActivityEvents = null;
		String currentActivityName = null;
		for (int iLine = startIndex; iLine < EventLines.size(); iLine++) {
			List<String> EventLine = EventLines.get(iLine);
			String activityName = EventLine.get(0);
			
			// if an event for a new activity has arrived, then the previous activity is done, and new
			// events on that activity will start a new sequence.  The previous activity is set to the current
			// activity and the current activity is set to the new activity.  We also re-get the activity event list
			// since we may be returning to the previous activity.
			if (currentActivityName == null) {
				currentActivityName = activityName;
			} else if (!activityName.equals(currentActivityName)) {
				if (previousActivityEvents != null) {
					previousActivityEvents.setDone(true);
				}
				previousActivityEvents = getActivityEventList(currentActivityName, activityEventList);
				currentActivityName = activityName;
			}
			
			ActivityEvents activityEvents = getActivityEventList(activityName, activityEventList);			
			if (activityEvents == null) {
				activityEvents = new ActivityEvents(activityName);
				activityEventList.add(activityEvents);
			}
			activityEvents.addEvents(EventLine);
		}
	}
	
	/**
	 * have we already put this activity into the activity Event list and is it still receiving events
	 * in this sequence?  We want the LAST entry, since we may be setting the "done" pointer.  In theory, it
	 * will pick up the correct one because we set the done marker, but let's check the lock twice.
	 */
	public static ActivityEvents getActivityEventList(String activityName, List<ActivityEvents> activityEventList) {
		ActivityEvents result = null;
		for (ActivityEvents activityEvent : activityEventList) {
			if (activityEvent.getActivityName().equals(activityName) && !activityEvent.isDone()) {
				result = activityEvent;
			}
		}
		return result;
	}

}
