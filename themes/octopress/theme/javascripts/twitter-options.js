/*-
script: true
 -*/
jQuery(function($){
  jQuery(".tweet").tweet({
	username: "${site.asides.tweets.user}",
	count: "${site.asides.tweets.count ?: 2}",
	seconds_ago_text: "about %d seconds ago",
	a_minutes_ago_text: "about a minute ago",
	minutes_ago_text: "about %d minutes ago",
	a_hours_ago_text: "about an hour ago",
	hours_ago_text: "about %d hours ago",
	a_day_ago_text: "about a day ago",
	days_ago_text: "about %d days ago",
	view_text: "view tweet on twitter"
	});
});
