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

/**
 * Allow user to change background color to another pastel.
 */
function pickPastelColor() {
  const pastels =
      ['#fac0e5', '#beebfa', '#fae5c0', '#ebc7ff', '#fff9c7', "#caf5e3", "#f7c3be", "#e2f7cb"];

  // Pick a pastel color.
  const pastel = pastels[Math.floor(Math.random() * pastels.length)];

  // Make the background color change.
  const body = document.getElementById('body');
  body.style.background = pastel;
}
// function getRandomQuoteUsingArrowFunctions() {
//   fetch('/random-quote').then(response => response.text()).then((quote) => {
//     document.getElementById('quote-container').innerText = quote;
//   });
// }
// function getRandomFactUsingArrowFunctions() {
//   fetch('/data').then(response => response.text()).then((fact) => {
//     document.getElementById('fact-container').innerText = fact;
//   });
// }
// function getServletData() {
//   fetch('/data').then(response => response.json()).then((fact) => {
//     document.getElementById('json-container').innerText = fact;
//   });
// }

function getCommentData() {
  document.getElementById('fetch-data').innerHTML = "";
  fetch('/data').then(response => response.json()).then((messages) => {
    const taskListElement = document.getElementById('fetch-data');
    messages.forEach((task) => {
      taskListElement.appendChild(createListElement(task));
    })
  });
}
function createListElement(text) {
  const liElement = document.createElement('h1');
  liElement.innerText = text;
  return liElement;
}

