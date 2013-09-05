package com.androidApp.emitter;

import java.util.ArrayList;
import java.util.List;

import com.androidApp.util.Constants;

public class OrderByActivity {
	protected class ActivityTokens {
		String mActivityName;
		List<List<String>> mTokenLines;
		
		public ActivityTokens(String activityName) {
			mActivityName = activityName;
			mTokenLines = new ArrayList<List<String>>();
		}
		
		public void addTokens(List<String> tokens) {
			mTokenLines.add(tokens);
		}
		
		public String getActivityName() {
			return mActivityName;
		}
		
		public List<List<String>> getTokenLines() {
			return mTokenLines;
		}
	}
	
	public List<List<String>> orderTokensByActivity(List<List<String>> tokenLines) {
		List<ActivityTokens> activityTokenList = new ArrayList<ActivityTokens>();
		orderTokensByActivity(tokenLines, activityTokenList, 0);
		return outputOrderedTokens(activityTokenList);
	}
	
	public List<List<String>> outputOrderedTokens(List<ActivityTokens>	 activityTokenList) {
		List<List<String>> outputTokens = new ArrayList<List<String>>();
		for (ActivityTokens activityTokens : activityTokenList) {
			for (List<String> tokenLine : activityTokens.getTokenLines()) {
				outputTokens.add(tokenLine.subList(1,  tokenLine.size()));
			}			
		}
		return outputTokens;
	}
	
	public void orderTokensByActivity(List<List<String>> 	 tokenLines, 
									  List<ActivityTokens>	 activityTokenList,
									  int					 startIndex) {
		ActivityTokens activityTokens = null;
		String currentActivity = null;
		for (int iLine = startIndex; iLine < tokenLines.size(); iLine++) {
			List<String> tokenLine = tokenLines.get(iLine);
			String activityName = tokenLine.get(0);
			String action = tokenLine.get(1);
			if (Constants.ActivityEvent.isActivityEvent(action)) {
				if (activityTokens == null) {
					currentActivity = activityName;
					activityTokens = new ActivityTokens(currentActivity);
					activityTokenList.add(activityTokens);
					activityTokens.addTokens(tokenLine);
				} else {
					// start of new activity sequence ends current sequence.
					if (!activityName.equals(currentActivity)) {
						orderTokensByActivity(tokenLines, activityTokenList, iLine);
						break;
					} else {
						activityTokens.addTokens(tokenLine);						
					}
				}
			} else {
				if (activityTokens != null) {
					activityTokens.addTokens(tokenLine);
				}
			}
		}
	}
}
