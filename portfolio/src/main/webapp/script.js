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

window.onload =
    function onLoad() {
  getComments();
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
  document.getElementById('comments-form').style.display = 'block';
}

/**
 * Fetches a list of comments and displays them on the UI.
 */
function getComments() {
  fetch('/data').then(response => response.json()).then((comments) => {
    let commentsContainer = document.getElementById('comments-container');
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
