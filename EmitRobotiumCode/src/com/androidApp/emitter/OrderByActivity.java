package com.androidApp.emitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
		
		public void addEvent(List<String> event) {
			mEventLines.add(event);
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
		
		public String toString() {
			return mActivityName;
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
			for (List<String> eventLine : ActivityEvents.getEventLines()) {
				System.out.println(eventLine.toString());
				outputEvents.add(eventLine.subList(1,  eventLine.size()));
			}			
		}
		return outputEvents;
	}
	
	/**
	 * take the activity-intereaved list of Eventized events, ond return a list of events ordered by activity
	 */
	public void orderEventsByActivity(List<List<String>> 	 eventLines, 
									  List<ActivityEvents>	 activityEventList,
									  int					 startIndex) {
		// push and pop activities based on activity_forward/back/back_key/finish
		Stack<ActivityEvents>	activityStack = new Stack<ActivityEvents>();
		String currentActivity = null;
		for (List<String> eventLine : eventLines) {
			String activity = eventLine.get(0);
			String action = eventLine.get(1);
			if (Constants.ActivityEvent.ACTIVITY_FORWARD.equals(action)) {
				//  push the activity onto the stack and add the forward event.
				ActivityEvents activityEvents = new ActivityEvents(activity);
				activityEvents.addEvent(eventLine);
				activityStack.push(activityEvents);	
				currentActivity = activity;
			} else if (Constants.ActivityEvent.ACTIVITY_BACK.equals(action) ||
					   Constants.UserEvent.ACTIVITY_BACK_KEY.equals(action)) {
				// activity_back is actually registered to the activity going back TO, not the activity back from.
				// normally, I'd just pop it, but activities not on top of the stack can be finished. Also, don't forget to add the actual event.
				if (inActivityStack(activity, activityStack)) {
					ActivityEvents activityEvents = getFromActivityStack(activity, activityStack);
					activityEvents.addEvent(eventLine);
					System.out.println("activity " + currentActivity + " adding " + activityEvents.getEventLines().size() + " events ");
					ActivityEvents currentActivityEvents = getFromActivityStack(currentActivity, activityStack);
					activityEventList.add(currentActivityEvents);
					removeFromActivityStack(currentActivity, activityStack);
					currentActivity = activity;
				}
				
			// back activity_finish is registered for the actual activity.
			} else if (Constants.ActivityEvent.ACTIVITY_FINISH.equals(action)) {
				if (inActivityStack(activity, activityStack)) {
					ActivityEvents activityEvents = getFromActivityStack(activity, activityStack);
					activityEvents.addEvent(eventLine);
					System.out.println("activity " + activity + " adding " + activityEvents.getEventLines().size() + " events ");
					activityEventList.add(activityEvents);
					removeFromActivityStack(activity, activityStack);
				}
			} else {
				
				// normal case, add the events to the activity
				ActivityEvents activityEvents = getFromActivityStack(activity, activityStack);
				if (activityEvents != null) {
					activityEvents.addEvent(eventLine);
				} else {
					System.out.println("failed to find activity " + activity + " in " + activityStack);
				}
			}
		}
		while (!activityStack.isEmpty()) {
			ActivityEvents activityEvents= activityStack.pop();
			activityEventList.add(activityEvents);
		}
	}
	
	/**
	 * is this activity name in the activity stack?
	 * @param activity activity to search for
	 * @param activityStack current activity stack
	 * @return
	 */
	protected boolean inActivityStack(String activity, Stack<ActivityEvents> activityStack) {
		for (ActivityEvents activityEvents : activityStack) {
			if (activityEvents.getActivityName().equals(activity)) {
				return true;
			}
		}
		return false;
	}
	
	protected ActivityEvents getFromActivityStack(String activity, Stack<ActivityEvents> activityStack) {
		ActivityEvents activityEventMatch = null;
		for (ActivityEvents activityEvents : activityStack) {
			if (activityEvents.getActivityName().equals(activity)) {
				activityEventMatch = activityEvents;
			}
		}
		return activityEventMatch;
	}

	/**
	 * remove an activity from the stack
	 * @param activity activity to search for
	 * @param activityStack current activity stack
	 */
	protected boolean removeFromActivityStack(String activity, Stack<ActivityEvents> activityStack) {
		ActivityEvents activityEventMatch = null;
		for (ActivityEvents activityEvents : activityStack) {
			if (activityEvents.getActivityName().equals(activity)) {
				activityEventMatch = activityEvents;
			}
		}
		if (activityEventMatch != null) {
			activityStack.remove(activityEventMatch);
			return true;
		}
		return false;
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
