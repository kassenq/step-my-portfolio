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

function getCommentData() {
  document.getElementById('fetch').innerHTML = "";
  var num = document.getElementById("num");
  num = num.options[num.selectedIndex].value;
  var url = "/data?max=" + num; 

  fetch(url).then(response => response.json()).then((comments) => {
    const commentListElement = document.getElementById('fetch');
    comments.forEach((comment) => {
      commentListElement.appendChild(createListElement(comment.name, comment.text));
    })
  });
}

function createListElement(name, text) {
  const liElement = document.createElement('p');
  liElement.innerText = name + ': ' + text;
  return liElement;
}



