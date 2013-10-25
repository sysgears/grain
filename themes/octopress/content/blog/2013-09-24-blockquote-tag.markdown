---
layout: post
title: Blockqoute tag
date: 2013-09-24 11:35
author: SysGears
categories: [grain, groovy]
comments: true
published: true
---

<!--more-->

<%= blockquote content:
'''Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor
incididunt ut labore et dolore magna aliqua.'''
%>

####Quote from a printed work

<%= blockquote author: 'John Doe', sourceTitle: 'Lorem ipsum', content:
'''Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor
incididunt ut labore et dolore magna aliqua.'''
%>

####Quote from Twitter

<%= blockquote author: 'John Doe', sourceLink: 'http://example.com', content:
'''Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor
incididunt ut labore et dolore magna aliqua.'''
%>

####Quote from a post

<%= blockquote author: 'John Doe', sourceTitle: 'Lorem ipsum', sourceLink: 'http://example.com', content:
'''Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor
incididunt ut labore et dolore magna aliqua.'''
%>