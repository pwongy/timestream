# Timestream

## About
Timestream is an Android app for recording **irregularly recurring events**, often of low priority and without specific deadlines. We humans tend to forget these easily. The app helps users keep track of these events and provides reminders when they are "due".

Examples of things you may want to log include:
- Recording medical symptoms (e.g. migraines, back pain) or periods (to check for regularity)
- Logging check-ups with your doctor/dentist (e.g. blood tests)
- Tracking sobriety
- Replacing your Britta filter
- Updating account passwords
- Clearing cookies/internet cache
- Calling your mum/dad
- Buying flowers for your girlfriend
- Starting a new pair of contact lenses/razor blade/toothbrush
- Getting a haircut
- Washing the car

## Development Plan
Subject to change. Current thoughts as follows:
- We are using Realm for data storage. Tables will be queried on the fly for displaying the data.
- The home screen will show a chronological list of occurred events in a form similar to [Material Design steppers](https://material.io/guidelines/components/steppers.html).
- Events will be added via either a separate Activity or using a BottomSheet, similar to the Todoist Android app.

*A Nightcap Initiative*