Project based on: https://fivethirtyeight.com/features/can-you-split-the-states/

Suppose you remove a set of states (not necessarily four) so that you have two distinct contiguous regions among the lower 48 states, where the larger region has area A and the smaller region has area B. What states should you remove to maximize area B? (Was Philip’s intuition correct?) And what percentage of the lower 48 states’ combined area does B represent?

My answer: IL, MO, NE, CO, NM. Results in smaller half having 43.07% of the total area.

Submitted answers published on FTE: https://fivethirtyeight.com/features/can-you-bowl-three-strikes/
- 42.65% removing ID, WY, NE, MO, AR, LA
- 42.73% removing NM, OK, MO, IA, MN

Funnily enough, when removing 6 states, like the first published answer did, the best removal was my original 5 and RI (the smallest state) even though RI isn't contiguous with the other removals. So, it seems my solution is better than the published ones :)

While my 5-removal has a larger smaller part than my 6-removal (43.07673% vs. 43.07237%), my 6-removal has a 0.01% difference between the two areas when the 5-removal has a 0.1% difference.
By removing RI, the smaller half becomes the east half when it was the west half before.

Since the 6-removal didn't find a better path than the 5-removal, I have a strong reason to believe that my 5-removal was optimal (i.e. a 7 or more removal won't do better since it will reduce the area).
