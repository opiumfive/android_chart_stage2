<b>Telegram April contest 2019. (1 week) (Result - <i>4th place</i> due to only 1 of 5 bonus goal)</b> 

(March contest - https://github.com/opiumfive/android_chart_stage1)

Goal is to show simple charts with 5 bonuses (I did only last):
1. A line chart with 2 lines, exactly like in Stage 1
2. A line chart with 2 lines and 2 Y axes
3. A stacked bar chart with 7 data types
4. A daily bar chart with single data type
5. A percentage stacked area chart with 6 data types
Bonus goal: A percentage stacked area chart with 6 data types that zooms into a pie chart with average values for the selected period

"When selecting the winners of Stage 2, we will consider speed, attention to detail and functionality. "

<b>Result apk:</b> 

https://github.com/opiumfive/android_chart_stage2/blob/master/TChart-v2.0.apk

Code for renderer is just a god object for all 4 types of charts, it was only 1 week and almost no free time to make code well constructed.

<b>Small result video:</b> 

Overall: https://drive.google.com/open?id=1ISAEmVd3YekvpU1Z5BrJZfNqJjVwtmsK

Area to pie transition: https://github.com/opiumfive/android_chart_stage2/raw/master/area_to_pie_video.mp4

<b>Some notes additional to part 1:</b> 
- still on canvas;
- reworked y animations;
- optimized performance for CPU-based drawing functions by bitmap caching;
- for area to pie chart transition "canvas.drawBitmapMesh()" function used, btw not well documented;
- optimized for landscape mode
- apk size 120kb with 50kb json data

<b>Public review:</b>
https://contest.dev/chart-android/entry177

<b>Todo things:</b>
- optimize performance of area by using drawColor-clipPath
- remove antialiasing from several types of chart
- make proper animation of filter button
