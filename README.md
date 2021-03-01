# Walmart Vaccine Portal

## Why?
Have you tried finding a Walmart in your state with available vaccine appointments? It sucks. See PROBLEM.md

## What can we do?
Make a simple website that shows you the nearest store in your state with a vaccine.

## What does this repo do?

1. Given a Walmart Store ID, get available appointments in the next 7 days.
2. Given a Walmart Store ID, get the address.
3. Given 2 addresses, get the route.

## What next?
Ideally, in my mind, there is a JSON file updated as frequently as possible containing an index of all Walmart Store IDs with appointments, indexed by state, and including their zip codes.

There is another JSON file per store id with the full address and available appointments.

A webpage fetches the index, identifies the user's state through user-input/geo-location, fetches the specific store files sorted by distance.

## PROBLEMS
1. Programmatically get and refresh the minimum necessary cookie to communicate with Walmart's API.
2. Some backend stuff to make use of 1.
