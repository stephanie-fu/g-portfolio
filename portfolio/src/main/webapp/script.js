// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

window.onload = function onLoad() {
  getComments('en');
}

/**
 * Opens tab corresponding to courseName.
 * @param {object} evt        Click event
 * @param {String} courseName Tab ID to be opened
 */
function openCourses(evt, courseName) {
  let tabcontent, tablinks;

  tabcontent = document.getElementsByClassName('tabcontent');
  for (let i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = 'none';
  }

  tablinks = document.getElementsByClassName('tablinks');
  for (let i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(' active', '');
  }

  document.getElementById(courseName).style.display = 'block';
  evt.currentTarget.className += ' active';
}

/**
 * Makes comments form visible.
 */
function showCommentsForm() {
  isLoggedIn().then((loggedIn) => {
    if (loggedIn) {
      document.getElementById('comments-form').style.display = 'block';
    }
    else {
      window.location.replace("/login");
    }
  });
}

/**
 * Fetches a list of comments and displays them on the UI.
 */
function getComments(language) {
  fetch('/data?' + new URLSearchParams({lang: language}))
  .then(response => response.json()).then((comments) => {
    let commentsContainer = document.getElementById('comments-container');
    commentsContainer.innerHTML = '';
    for (let i = 0; i < comments.length; i++) {
      commentsContainer.appendChild(createListElement(comments[i]));
    }
  });
}

/**
 * Creates an <li> element containing text.
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/** 
 * Creates a chart and adds it to the page. 
 */
function drawChart() {
  const majorityColor = '#045877';
  const minorityColor = '#89DAFF';

  const charts = [
    google.visualization.arrayToDataTable([
      ['Trait', 'Extraverted', { role: 'style' }, {role: 'annotation'}, 
                'Introverted', { role: 'style' }, {role: 'annotation'}],
      ['Mind', .11, minorityColor, 'Extraverted', .89, majorityColor, 'Introverted']
    ]), 
    google.visualization.arrayToDataTable([
      ['Trait', 'Intuitive', { role: 'style' }, {role: 'annotation'}, 
                'Observant', { role: 'style' }, {role: 'annotation'}],
      ['Energy', .66, majorityColor, 'Intuitive', .34, minorityColor, 'Observant']
    ]), 
    google.visualization.arrayToDataTable([
      ['Trait', 'Thinking', { role: 'style' }, {role: 'annotation'}, 
                'Feeling', { role: 'style' }, {role: 'annotation'}],
      ['Nature', .89, majorityColor, 'Thinking', .11, minorityColor, 'Feeling']
    ]), 
    google.visualization.arrayToDataTable([
      ['Trait', 'Judging', { role: 'style' }, {role: 'annotation'}, 
                'Prospecting', { role: 'style' }, {role: 'annotation'}],
      ['Tactics', .54, majorityColor, 'Judging', .46, minorityColor, 'Prospecting']
    ]), 
    google.visualization.arrayToDataTable([
      ['Trait', 'Assertive', { role: 'style' }, {role: 'annotation'}, 
                'Turbulent', { role: 'style' }, {role: 'annotation'}],
      ['Identity', .24, minorityColor, 'Assertive', .76, majorityColor, 'Turbulent']
    ])
  ];

  const options = {
    backgroundColor: 'transparent', 
    hAxis: { textPosition: 'none' },
    isStacked: 'percent', 
    legend: 'none', 
  };

  for (let i = 0; i < charts.length; i++) {
    new google.visualization.BarChart(
        document.getElementById(`mbti-chart-${i}`)).draw(charts[i], options);
  }
}

async function isLoggedIn() {
  let response = await fetch('/login');
  return response.ok;
}
