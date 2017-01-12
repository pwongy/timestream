# Timestream

## About
Timestream is an Android app for recording irregularly recurring events, often of low priority. We humans can forget these easily, so this app is designed to help keep track of these events and provide reminders when they are "due".

Examples of things you may want to log include:
- Replacing your water filter
- Seeing the doctor/dentist
- Recording periods (to check for regularity), or medical symptoms (e.g. migraines, back pain)
- Starting a new pair of contact lenses / razor blade / toothbrush
- Calling your mum/dad
- Getting a haircut
- Washing the car

## Development Plan
- We are using Realm for data storage. Tables will be queried on the fly for displaying the data.
- The home screen will show a chronological list of occurred events in a form similar to [Material Design steppers](https://material.io/guidelines/components/steppers.html).
- Events will be added via either a separate Activity or using a BottomSheet, similar to the Todoist Android app.

_A Nightcap Initiative_