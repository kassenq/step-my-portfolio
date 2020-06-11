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

const COMMENT_CHILD_TAG = 'p';
const POST = 'POST';
const FETCH_ID = 'fetch';
const BODY_ID = 'body';

/**
 * Allow user to change background color to another pastel.
 */
function pickPastelColor() {
  const pastels = [
    '#fac0e5', '#beebfa', '#fae5c0', '#ebc7ff', '#fff9c7', '#caf5e3', '#f7c3be',
    '#e2f7cb'
  ];

  // Pick a pastel color.
  const pastel = pastels[Math.floor(Math.random() * pastels.length)];

  // Make the background color change.
  const body = document.getElementById(BODY_ID);
  body.style.background = pastel;
}

/**
 * Get # of comments user would like to display and adds each comment as a list
 * entry.
 */
function getCommentData() {
  document.getElementById(FETCH_ID).innerHTML = '';
  var num = document.getElementById('num');
  num = num.options[num.selectedIndex].value;
  var url = '/data?max=' + num;

  fetch(url).then(response => response.json()).then((comments) => {
    const commentListElement = document.getElementById(FETCH_ID);
    comments.forEach((comment) => {
      commentListElement.appendChild(
          createListElement(comment.email, comment.text, comment.imageUrl));
    })
  });
}

/**
 * Create a list element for comments to be formatted when displayed.
 */
function createListElement(email, text, imageUrl) {
  const liElement = document.createElement(COMMENT_CHILD_TAG);
  const imgElement = document.createElement('img');
  imgElement.src = imageUrl;
  liElement.innerText = email + ': ' + text;
  liElement.appendChild(imgElement);
  return liElement;
}

/**
 * Creates POST request to delete existing comments.
 */
function deleteCommentData() {
  const request = new Request('/delete-data', {method: POST});
  fetch(request).then(response => getCommentData());
}

/**
 * Fetch user login status from LoginServlet.
 */
function getLoginStatus() {
  fetch('/login').then(response => response.json()).then((userStatus) => {
    if (userStatus.isLoggedIn) {
      document.getElementById('delete-button-div').style.display = 'show';
      document.getElementById('comments-form').style.display = 'show';
      document.getElementById('handle-login').innerHTML =
          userStatus.loginMessage;
    } else {
      document.getElementById('comments-form').style.display = 'none';
      document.getElementById('delete-button-div').style.display = 'none';
      const messageElement = document.createElement(COMMENT_CHILD_TAG);
      messageElement.innerText = userStatus.loginMessage;
      document.getElementById('handle-login').innerHTML =
          messageElement.innerText;
    }
  });
}

/**
 * Fetch URL of user-uploaded image from Blobstore servlet.
 */
function fetchBlobstoreUrl() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        console.log(response);
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('comments-form');
        messageForm.action = imageUploadUrl;
        messageForm.classList.remove('hidden');
      });
}

/**
 * Calls all functions necessary to initialize page.
 */
function initializePage() {
  getLoginStatus();
  fetchBlobstoreUrl();
}
