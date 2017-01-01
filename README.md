# Timestream

_A Nightcap Initiative._

## About

**Timestream** is an Android app for recording irregularly recurring events (i.e. those without a
set period) that are often of low priority. We humans can forget these easily, so this app is
designed to help keep track of these tasks and provide reminders when they are "due".

Examples of things you may want to log include:
- Seeing the doctor/dentist
- Recording periods (to check for regularity), or symptoms (e.g. migraines, back pain)
- Changing contacts/razor blades/toothbrushes
- Getting a haircut
- Washing the car

## Development Plan

- We are using Realm for data storage. For simplicity, there will be a single table for logging all
events. This table will be queried on the fly for displaying the data.
- The home screen will show a chronological list of occurred events in a form similar to [Material
Design steppers](see https://material.io/guidelines/components/steppers.html). This will require
the use of a custom ListAdapter.
- Events will be added via either a separate Activity or using a BottomSheet, similar to the Todoist
Android app.