function onChangeSelectHandler() {
    console.log("works really fine");
    var testDivEl = document.getElementById("testDiv");
    var selectEl = testDiv.getElementsByTagName("select");
    console.log("selectEl", selectEl[0].options);

    var options = selectEl[0].options;

    var generatedUlDivEl = document.getElementById("generatedUlDiv");
    var generatedUl = '<ul id="generatedUl">';
    for (i = 0; i < options.length; i++) {
        generatedUl += '<li>'+ options[i].label+'</li>';
    }
    generatedUl += '</ul>';
    generatedUlDivEl.innerHTML = generatedUl;

    var searchInputEl = document.getElementById("searchInputDiv");
    searchInputEl.innerHTML = '<input type="text" name="searchInput" value="" class="setting-input"/>';

};

function onClickResultHandler() {
    var toggleEl = document.getElementById("hiddenSelect");
    console.log("click!", toggleEl.style.display);
    if (toggleEl.style.display === "block") {
        toggleEl.style.display = "none";
    } else {
        toggleEl.style.display = "block";
    }
};

//onChangeSelectHandler();