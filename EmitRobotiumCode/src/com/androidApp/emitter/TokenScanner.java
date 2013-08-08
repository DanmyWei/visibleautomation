package com.androidApp.emitter;

import java.util.List;

import com.androidApp.util.Constants;

/**
 * we pre-tokenize the event file so we can do forward and reverse scanning while the events
 * are parsed into output code. Suppose for example, we hit a button which causes an activity
 * transition, but before the transition, it clears some text controls.  We actually want to
 * not emit code for those events, because even though they were recorded, we can't test them
 * quickly enough on playback before the activity transition, and they would fail.
 * @author matt2
 *
 */
public class TokenScanner {
	public interface Predicate {
		boolean test(List<String> tokens);
	}
	
	/**
	 * scan forward for the line which matches the passed predicate
	 * @param lines tokenized lines
	 * @param startIndex starting index
	 * @param predicate predicate to test
	 * @return
	 */
	public static int scanForward(List<List<String>> lines, int startIndex, Predicate predicate) {
		for (int i = startIndex; i < lines.size(); i++) {
			List<String> tokens = lines.get(i);
			if (predicate.test(tokens)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * scan backward for the line which matches the passed predicate
	 * @param lines tokenized lines
	 * @param startIndex starting index
	 * @param predicate predicate to test
	 * @return
	 */
	
	public static int scanBackward(List<List<String>> lines, int startIndex, Predicate predicate) {
		for (int i = startIndex; i >= 0; i--) {
			List<String> tokens = lines.get(i);
			if (predicate.test(tokens)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * does predicate1 succeed before Predicate2 in a forward scan?
	 * @param lines list of token lists representing events.
	 * @param startIndex start index to scan from
	 * @param before must be true before 
	 * @param after must be true after.
	 * @return
	 */
	public static boolean happensBefore(List<List<String>> 	lines, 
										int 				startIndex, 
										Predicate 			before, 
										Predicate 			after) {
		boolean beforeHappened = false;
		for (int i = startIndex; i < lines.size(); i++) {
			List<String> tokens = lines.get(i);
			if (!beforeHappened) {
				beforeHappened = before.test(tokens);
			} else {
				if (after.test(tokens)) {
					return true;
				}
			}
		}
		return false;		
	}
	
	public static int findPrevious(List<List<String>> lines, int startIndex, Predicate predicate) {
		for (int i = startIndex - 1; i >= 0; i--) {
			List<String> tokens = lines.get(i);
			if (predicate.test(tokens)) {
				return i;
			}
		}
		return -1;
	}
										  
	
	/**
	 * simple predicate for "was this event sent by the user?"
	 * @author matt2
	 *
	 */
	public class UserEventPredicate implements Predicate {
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			return Constants.UserEvent.isUserEvent(action);
		}
	}
	
	/**
	 * simple predicate for "was this event sent by the system?
	 * @author matt2
	 *
	 */
	public class SystemEventPredicate implements Predicate {
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			return Constants.SystemEvent.isSystemEvent(action);
		}
	}
	
	/**
	 * is predicate a or b true?
	 * @author matt2
	 *
	 */
	public class OrPredicate implements Predicate {
		Predicate mA;
		Predicate mB;
		
		public OrPredicate(Predicate a, Predicate b) {
			mA = a;
			mB = b;
		}
		public boolean test(List<String> tokens) {
			return mA.test(tokens) || mB.test(tokens);
		}
	}
	
	/**
	 * is this predicate one of the lists of events?
	 * @author matt2
	 *
	 */
	public class EventListPredicate implements Predicate {
		protected String[] mEvents;
		
		public EventListPredicate(String[] events) {
			mEvents = events;
		}
		public EventListPredicate(String event) {
			mEvents = new String[1];
			mEvents[0] = event;
		}
	
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			for (String event : mEvents) {
				if (event.equals(action)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * match an event with the specified value at the specified index.
	 * "interstitial_activity",2, "MyActivityName"
	 * @author matt2
	 *
	 */
	public class EventParameterPredicate implements Predicate {
		protected String mEvent;
		protected int mParameterIndex;
		protected String mValue;
		
		public EventParameterPredicate(String event, int index, String value) {
			mEvent = event;
			mParameterIndex = index;
			mValue = value;
		}
		
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			if (action.equals(mEvent)) {
				if (mParameterIndex < tokens.size()) {
					String candValue = tokens.get(mParameterIndex);
					return candValue.equals(mValue);
				}
			}
			return false;
		}
	}
	
	/**
	 * test a predicate, then a parameter for tokens which have passed the predicate.
	 * @author matt2
	 *
	 */
	public class PredicateParameter implements Predicate {
		protected Predicate mPredicate;
		protected int 		mParameterIndex;
		protected String 	mValue;
		protected boolean	mfMatch;
		
		public PredicateParameter(Predicate predicate, int index, String value) {
			mPredicate = predicate;
			mParameterIndex = index;
			mValue = value;
			mfMatch = true;
		}
		
		public PredicateParameter(Predicate predicate, int index, String value, boolean fMatch) {
			mPredicate = predicate;
			mParameterIndex = index;
			mValue = value;
			mfMatch = fMatch;
		}
		
		// true or false test on the argument match
		public boolean test(List<String> tokens) {
			String action = tokens.get(0);
			if (mPredicate.test(tokens)) {
				if (mParameterIndex < tokens.size()) {
					String candValue = tokens.get(mParameterIndex);
					if (mfMatch) {
						return candValue.equals(mValue);
					} else {
						return !candValue.equals(mValue);
					}
				}
			}
			return false;
		}
	}
	
		
	public class ActivityTransitionPredicate extends EventListPredicate {
		public ActivityTransitionPredicate() {
			super(new String[] { Constants.ActivityEvent.ACTIVITY_BACK.mEventName,
								 Constants.ActivityEvent.ACTIVITY_FORWARD.mEventName,
								 Constants.UserEvent.ACTIVITY_BACK_KEY.mEventName });
		}
	}

	
	public class InterstitialDialogPredicate extends EventListPredicate {
		public InterstitialDialogPredicate() {
			super(new String[] { 
					Constants.UserEvent.INTERSTITIAL_DIALOG_TITLE_ID.mEventName,
					Constants.UserEvent.INTERSTITIAL_DIALOG_TITLE_TEXT.mEventName,
					Constants.UserEvent.INTERSTITIAL_DIALOG_CONTENTS_ID.mEventName,
					Constants.UserEvent.INTERSTITIAL_DIALOG_CONTENTS_TEXT.mEventName });
		}
	}
	
	public class DialogClosePredicate extends EventListPredicate {
		public DialogClosePredicate() {
			super(new String[] {
					Constants.SystemEvent.DISMISS_DIALOG.mEventName,
					Constants.UserEvent.CANCEL_DIALOG.mEventName,
					Constants.UserEvent.DISMISS_DIALOG_BACK_KEY.mEventName});
		}
	}
	
}
