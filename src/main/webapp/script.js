function onChangeSelectHandler() {
    var selectEl = getSelectEl();
    var options = selectEl.options;

    var generatedUlDivEl = document.getElementById("generatedUlDiv");
    var generatedUl = '<ul id="generatedUl" data-display-elements="' + options.length + '">';
    for (i = 0; i < options.length; i++) {
        generatedUl += '<li class="generatedLi" onclick="onClickListElementHandler(this)" '
                    + ' onmouseover="focusLi(this)" onmouseout="clearFocusLi(this)" '
                    + 'data-value="' + options[i].value
                    + '" data-index="' + i
                    + '" data-display-index="' + i
                    + '" >' + options[i].label + '</li>';

        if (options[i].selected) {
            setNameToResult(options[i].label);
        }
    }
    generatedUl += '</ul>';
    generatedUlDivEl.innerHTML = generatedUl;

    var searchInputDivEl = document.getElementById("searchInputDiv");
    searchInputDivEl.innerHTML = '<input type="text" onkeyup="onKeyUpSearch()" id="searchInput" name="searchInput" value="" class="setting-input" placeholder="Search Tests..."/>';
};

var selectEl = getSelectEl();
setNameToResult(selectEl.getAttribute("value"));

function getSelectEl() {
    var testDivEl = document.getElementById("testDiv");
    return testDiv.getElementsByTagName("select")[0];
};

// waiting when init state of original <select> will be changed
var count = 0;
var interval = setInterval(function() {
    var resultDivEl = document.getElementById("result");
    var prevValue = resultDivEl.innerHTML;

    onChangeSelectHandler(); // if <select> element filled it will change 'resultDivEl.innerHTML'
    var curValue = resultDivEl.innerHTML;

    if ((prevValue != curValue) || (count >= 20)) {
        clearInterval(interval);
    }
    count++;
}, 500);

function onKeyUpSearch() {
    var searchInputEl = document.getElementById("searchInput");
    var text = searchInputEl.value.toLowerCase();
    var ulEl = document.getElementById("generatedUl");
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

function onClickResultHandler() {
    var toggleEl = document.getElementById("hiddenSelect");
    if (toggleEl.style.display === "block") {
        hideHiddenSelect();
    } else {
        onChangeSelectHandler();
        openHiddenSelect(toggleEl);
        disableScroll();
    }
};

function openHiddenSelect(toggleEl) {
    toggleEl.style.display = "block";
    var searchInputEl = document.getElementById("searchInput");
    searchInputEl.focus();
    var liList = document.getElementById("generatedUl").getElementsByTagName("li");
    var resultDivEl = document.getElementById("result");
    for (i = 0; i < liList.length; i++) {
        if (liList[i].innerHTML == resultDivEl.innerHTML) {
            focusLi(liList[i]);
            moveScroll(liList[i].dataset.displayIndex);
            return;
        }
    }
};

function onClickListElementHandler(li) {
    setNameToResult(li.innerHTML);
    setValueToSelect(li);
    hideHiddenSelect();
};

document.onmousedown = function(event) {
    if (!isGeneratedElement(event)) {
        hideHiddenSelect();
    }
};

function hideHiddenSelect() {
    var toggleEl = document.getElementById("hiddenSelect");
    toggleEl.style.display = "none";
    curFocusedLi = null;
    enableScroll();
};

function setValueToSelect(li) {
    var selectEl = getSelectEl();
    selectEl.setAttribute("value", li.dataset.value);
    selectEl.options[li.dataset.index].selected = true;
};

function setNameToResult(name) {
    var resultDivEl = document.getElementById("result");
    resultDivEl.innerHTML = name;
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
    var toggleEl = document.getElementById("hiddenSelect");
    if (toggleEl.style.display === "block") {
        if (isPressedDown(charCode)) {
            selectNextLi();
        } else if (isPressedUp(charCode)) {
            selectPrevLi();
        } else if (isPressedEnter(charCode)) {
            setCurrentLi();
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

function selectNextLi() {
    var currentIndex = (curFocusedLi == null) ? -1 : curFocusedLi.dataset.displayIndex;
    var nextIndex = ++currentIndex;
    var ulEl = document.getElementById("generatedUl");
    var liList = ulEl.getElementsByTagName("li");
    for (i = 0; i < liList.length; i++) {
        if (liList[i].dataset.displayIndex == nextIndex) {
            focusLi(liList[i]);
            moveScroll(nextIndex);
            return;
        }
    }
    selectFirst(liList);
};


// need use liList[i].dataset.displayIndex
function moveScroll(index) {
    var ulEl = document.getElementById("generatedUl");
    var elementsCount = ulEl.dataset.displayElements;
    var liList = ulEl.getElementsByTagName("li");
    if (liList.length > 0 && elementsCount > 0) {
        var liHeight = parseFloat(window.getComputedStyle(liList[0], null).getPropertyValue("height"));
        var ulHeight = parseFloat(window.getComputedStyle(ulEl, null).getPropertyValue("height"));
        var median = (ulHeight / liHeight / 2);
        if (index > median) {
            ulEl.scrollTop = (liHeight * (index - median));
        } else {
            ulEl.scrollTop = 0;
        }
    }
};

function selectFirst(liList) {
    for (i = 0; i < liList.length; i++) {
        if (liList[i].dataset.displayIndex != -1) {
            focusLi(liList[i]);
            moveScroll(liList[i].dataset.displayIndex);
            return;
        }
    }
};

function selectPrevLi() {
    var liList = document.getElementById("generatedUl").getElementsByTagName("li");
    if (curFocusedLi == null || curFocusedLi.dataset.displayIndex <= 0) {
        selectLast(liList);
        return;
    }
    var currentIndex = curFocusedLi.dataset.displayIndex;
    var prevIndex = --currentIndex;
    for (i = 0; i < liList.length; i++) {
        if (liList[i].dataset.displayIndex == prevIndex) {
            focusLi(liList[i]);
            moveScroll(prevIndex);
            return;
        }
    }
    selectLast(liList);
};

function selectLast(liList) {
    for (i = liList.length - 1; i >= 0; i--) {
        if (liList[i].dataset.displayIndex != -1) {
            focusLi(liList[i]);
            moveScroll(liList[i].dataset.displayIndex);
            return;
        }
    }
};

function setCurrentLi() {
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