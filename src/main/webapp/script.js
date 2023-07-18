function onChangeSelectHandler(event) {
   var target = event.target;
   var testDivEl = target.closest(".testDiv");
   onChangeSelectHandlerForTestDiv(testDivEl);
};


function onChangeSelectHandlerForTestDiv(testDiv) {
   var selectEl = getSelectElForTestDiv(testDiv);
   var options = selectEl.options;


   var generatedUlDivEl = testDiv.querySelector(".generatedUlDiv");
   var generatedUl = '<ul id="generatedUl" data-display-elements="' + options.length + '">';
   for (i = 0; i < options.length; i++) {
       generatedUl += '<li class="generatedLi" onclick="onClickListElementHandler(this)" '
                   + ' onmouseover="focusLi(this)" onmouseout="clearFocusLi(this)" '
                   + 'data-value="' + options[i].value
                   + '" data-index="' + i
                   + '" data-display-index="' + i
                   + '" >' + options[i].label + '</li>';


       if (options[i].selected) {
           setNameToResultFromTestDiv(testDiv, options[i].label);
       }
   }
   generatedUl += '</ul>';
   generatedUlDivEl.innerHTML = generatedUl;


   var searchInputDivEl = testDiv.querySelector(".searchInputDiv");
   searchInputDivEl.innerHTML = '<input type="text" onkeyup="onKeyUpSearch(this)" id="searchInput" name="searchInput" value="" class="setting-input" placeholder="Search Tests..."/>';
};


// START
setTimeout(function() {
   var testDivs = getAllTestDivs();
   for (var i = 0; i < testDivs.length; i++) {
       var div = testDivs[i];
       setNameToResultFromTestDiv(div, getSelectElForTestDiv(div).getAttribute("value"));
       executeIntervalTaskForDiv(div);
   }
}, 2000);
// end of START


function executeIntervalTaskForDiv(testDiv) {
   // waiting when init state of original <select> will be changed
   document.getElementById("hideLoader").style.display = "none";
   document.getElementById("box").style.display = "block";


   var count = 0;
   var prevValue;
   var isFirst = true;
   var interval = setInterval(function() {
       var resultDivEl = testDiv.querySelector("#result");
       if (isFirst) {
           prevValue = resultDivEl.innerHTML;
           isFirst = false;
       }


       onChangeSelectHandlerForTestDiv(testDiv); // if <select> element filled it will change 'resultDivEl.innerHTML'
       var curValue = resultDivEl.innerHTML;


       if (prevValue != curValue || count > 70) {
           document.getElementById("box").style.display = "none";
           document.getElementById("hideLoader").style.display = "block";
           clearInterval(interval);
       }
       count++;
   }, 500);
};


function setNameToResultFromTestDiv(testDiv, name) {
   var resultDivEl = testDiv.querySelector("#result");
   resultDivEl.innerHTML = name;
};


function getAllTestDivs() {
   return document.getElementsByClassName("testDiv");
};


function executeIntervalTask(selectEl) {
   var builderEl = selectEl.closest("div[descriptorid='hudson.plugins.blazemeter.PerformanceBuilder']");
   var testDivEl = builderEl.querySelector(".testDiv");
   executeIntervalTaskForDiv(testDivEl)
};




function getSelectElForTestDiv(testDivEl) {
   return testDivEl.getElementsByTagName("select")[0];
};




function onKeyUpSearch(searchInputEl) {
   var text = searchInputEl.value.toLowerCase();
   var testDivEl = searchInputEl.closest(".testDiv");
   var ulEl = testDivEl.querySelector("#generatedUl");
   var liList = ulEl.getElementsByTagName("li");
   var displayIndex = 0;
   for (i = 0; i < liList.length; i++) {
       if (!liList[i].innerHTML.toLowerCase().includes(text)) {
           liList[i].style.display = "none";
           liList[i].dataset.displayIndex = -1;
       } else {
           liList[i].style.display = "";
           liList[i].dataset.displayIndex = displayIndex;
           displayIndex++;
       }
   }
   ulEl.dataset.displayElements = displayIndex;
   if (curFocusedLi != null && curFocusedLi.dataset.displayIndex == -1) {
       clearFocusLi(curFocusedLi);
       curFocusedLi = null;
       ulEl.scrollTop = 0;
   }
};


function onClickResultHandler(resultDivEl) {
   var testDivEl = resultDivEl.closest(".testDiv");
   var toggleEl = testDivEl.querySelector(".hiddenSelect");
   if (toggleEl.style.display === "block") {
       hideHiddenSelect(toggleEl);
   } else {
       onChangeSelectHandlerForTestDiv(testDivEl);
       openHiddenSelect(testDivEl, toggleEl);
       disableScroll();
   }
};


function hideHiddenSelect(toggleEl) {
   if (toggleEl != null) {
       toggleEl.style.display = "none";
       curFocusedLi = null;
       enableScroll();
   }
};




function openHiddenSelect(testDivEl, toggleEl) {
   toggleEl.style.display = "block";
   var searchInputEl = toggleEl.querySelector("#searchInput");
   searchInputEl.focus();
   var generatedUlEl = toggleEl.querySelector("#generatedUl");
   var liList = generatedUlEl.getElementsByTagName("li");
   var resultDivEl = testDivEl.querySelector("#result");
   for (i = 0; i < liList.length; i++) {
       if (liList[i].innerHTML == resultDivEl.innerHTML) {
           focusLi(liList[i]);
           moveScroll(generatedUlEl, liList[i].dataset.displayIndex);
           return;
       }
   }
};


function onClickListElementHandler(li) {
   var testDivEl = li.closest(".testDiv");
   setNameToResultFromTestDiv(testDivEl, li.innerHTML);
   setValueToSelect(testDivEl, li);
   var toggleEl = testDivEl.querySelector(".hiddenSelect");
   hideHiddenSelect(toggleEl);
};


document.onmousedown = function(event) {
   if (!isGeneratedElement(event)) {
       var testDivs = getAllTestDivs();
       for (var i = 0; i < testDivs.length; i++) {
           var div = testDivs[i];
           var toggleEl = div.querySelector(".hiddenSelect");
           hideHiddenSelect(toggleEl);
       }
   }
};




function setValueToSelect(testDivEl, li) {
   var selectEl = getSelectElForTestDiv(testDivEl);
   selectEl.setAttribute("value", li.dataset.value);
   selectEl.options[li.dataset.index].selected = true;
};




function isGeneratedElement(event) {
   return (event.target.id == "result"
                  || event.target.id == "generatedUl"
                  || event.target.getAttribute("class") == "generatedLi"
                  || event.target.id == "searchInput");
};


document.onkeydown = function(event) {
   var charCode = event.keyCode;
   var focusedTag = document.activeElement.tagName;
   var targetInputEl = event.target;
   var toggleEl = targetInputEl.closest(".hiddenSelect");
   if (toggleEl != null && toggleEl.style.display === "block") {
       var generatedUlEl = toggleEl.querySelector("#generatedUl");
       if (isPressedDown(charCode)) {
           selectNextLi(generatedUlEl);
       } else if (isPressedUp(charCode)) {
           selectPrevLi(generatedUlEl);
       } else if (isPressedEnter(charCode)) {
           setCurrentLi(generatedUlEl);
       }
       preventDefaultForScrollKeys(event);
   }
};


function isPressedDown(charCode) {
   return (charCode === 40);
};


function isPressedUp(charCode) {
   return (charCode === 38);
};


function isPressedEnter(charCode) {
   return (charCode === 13);
};


var curFocusedLi = null;


// set style   background: #1E90FF;  color: #ffffff;
function focusLi(li) {
   li.style.background = "#1E90FF";
   li.style.color = "#ffffff";
   if (curFocusedLi != null) {
       clearFocusLi(curFocusedLi);
       curFocusedLi = null;
   }
   curFocusedLi = li;
};


// return default colors
function clearFocusLi(li) {
   li.style.background = "#ffffff";
   li.style.color = "#000000";
};


function selectNextLi(ulEl) {
   var currentIndex = (curFocusedLi == null) ? -1 : curFocusedLi.dataset.displayIndex;
   var nextIndex = ++currentIndex;
   var liList = ulEl.getElementsByTagName("li");
   for (i = 0; i < liList.length; i++) {
       if (liList[i].dataset.displayIndex == nextIndex) {
           focusLi(liList[i]);
           moveScroll(ulEl, nextIndex);
           return;
       }
   }
   selectFirst(ulEl, liList);
};




// need use liList[i].dataset.displayIndex
function moveScroll(ulEl, index) {
   var elementsCount = ulEl.dataset.displayElements;
   var liList = ulEl.getElementsByTagName("li");
   if (liList.length > 0 && elementsCount > 0) {
       var liHeight = parseFloat(getLiHeight(liList, index));
       var ulHeight = parseFloat(window.getComputedStyle(ulEl, null).getPropertyValue("height"));
       var median = (ulHeight / liHeight / 2);
       if (index > median) {
           ulEl.scrollTop = (liHeight * (index - median));
       } else {
           ulEl.scrollTop = 0;
       }
   }
};


function getLiHeight(liList, displayIndex) {
   for (i = 0; i < liList.length; i++) {
       if (liList[i].dataset.displayIndex == displayIndex) {
           return liList[i].clientHeight;
       }
   }


   var height = window.getComputedStyle(liList[0], null).getPropertyValue("height");
   if (height == 'auto') {
       return 19;
   } else {
       return height;
   }
};


function selectFirst(ulEl, liList) {
   for (i = 0; i < liList.length; i++) {
       if (liList[i].dataset.displayIndex != -1) {
           focusLi(liList[i]);
           moveScroll(ulEl, liList[i].dataset.displayIndex);
           return;
       }
   }
};


function selectPrevLi(ulEl) {
   var liList = ulEl.getElementsByTagName("li");
   if (curFocusedLi == null || curFocusedLi.dataset.displayIndex <= 0) {
       selectLast(ulEl, liList);
       return;
   }
   var currentIndex = curFocusedLi.dataset.displayIndex;
   var prevIndex = --currentIndex;
   for (i = 0; i < liList.length; i++) {
       if (liList[i].dataset.displayIndex == prevIndex) {
           focusLi(liList[i]);
           moveScroll(ulEl, prevIndex);
           return;
       }
   }
   selectLast(ulEl, liList);
};


function selectLast(ulEl, liList) {
   for (i = liList.length - 1; i >= 0; i--) {
       if (liList[i].dataset.displayIndex != -1) {
           focusLi(liList[i]);
           moveScroll(ulEl, liList[i].dataset.displayIndex);
           return;
       }
   }
};


function setCurrentLi(ulEl) {
   if (curFocusedLi != null) {
       onClickListElementHandler(curFocusedLi);
   }
};


// left: 37, up: 38, right: 39, down: 40,
// spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36
var keys = {37: 1, 38: 1, 39: 1, 40: 1, 13: 1};


function preventDefault(e) {
   e = e || window.event;
   if (e.preventDefault) {
       e.preventDefault();
   }
   e.returnValue = false;
};


function preventDefaultForScrollKeys(e) {
   if (keys[e.keyCode]) {
       preventDefault(e);
       return false;
   }
};


function disableScroll() {
   if (window.addEventListener) { // older FF
       window.addEventListener('DOMMouseScroll', preventDefault, false);
   }
};


function enableScroll() {
   if (window.removeEventListener) {
       window.removeEventListener('DOMMouseScroll', preventDefault, false);
   }
};
