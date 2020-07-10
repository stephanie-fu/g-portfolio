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
  getComments();
}

/**
 * Opens tab corresponding to courseName.
 * @param {object} evt        Click event
 * @param {String} courseName Tab ID to be opened
 */
function openCourses(evt, courseName) {
  const tabcontent = document.getElementsByClassName('tabcontent');
  for (let i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = 'none';
  }

  const tablinks = document.getElementsByClassName('tablinks');
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
function getComments() {
  const languageDropdown = $('#languages');
  const commentsContainer = document.getElementById('comments-container');
  const defaultLanguage = 'en';

  let sourceLanguage = languageDropdown.data('prev') || defaultLanguage;
  let targetLanguage = languageDropdown.val();
  
  fetch('/data?' + new URLSearchParams({'sourceLanguageCode': sourceLanguage, 
                                        'targetLanguageCode': targetLanguage}))
  .then(response => response.json()).then((comments) => {
    commentsContainer.innerHTML = '';
    for (let i = 0; i < comments.length; i++) {
      commentsContainer.appendChild(createListElement(comments[i]));
    }
    languageDropdown.data('prev', targetLanguage);
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
 * Creates a single chart based on one row of personality data
 * @param {Object} dataRow  Single-key object containing a 2D array of traits
 */
function createChart(dataRow) {
  const traitGroup = Object.keys(dataRow)[0];
  const traitArray = dataRow[traitGroup];

  const [firstTrait, firstValue] = traitArray[0];
  const [secondTrait, secondValue] = traitArray[1];

  let firstColor = '#045877';
  let secondColor = '#89DAFF';

  if (firstValue < secondValue) {
    [firstColor, secondColor] = [secondColor, firstColor];
  }

  return google.visualization.arrayToDataTable([
      ['', firstTrait, { role: 'style' }, {role: 'annotation'}, 
          secondTrait, { role: 'style' }, {role: 'annotation'}],
      ['', firstValue, firstColor, firstTrait, 
          secondValue, secondColor, secondTrait]
    ]);
}

/** 
 * Creates a chart and adds it to the page. 
 */
function drawChart() {

  const data = [
    {'Mind': [['Extraverted', .11], ['Introverted', .89]]}, 
    {'Energy': [['Intuitive', .66], ['Observant', .34]]}, 
    {'Nature': [['Thinking', .89], ['Feeling', .11]]}, 
    {'Tactics': [['Judging', .54], ['Prospecting', .46]]}, 
    {'Identity': [['Assertive', .24], ['Turbulent', .76]]}
  ]

  const charts = data.map(createChart);

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
