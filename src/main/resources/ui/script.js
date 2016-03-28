(function () {
  var nodeErrors = document.querySelector("#errors");
  var nodeErrorContent = document.querySelector("#errorsContent");
  var nodeErrorClear = document.querySelector("#errorClear");
  var nodeInit = document.querySelector("#init");
  var nodeCreate = document.querySelector("#create");
  var nodeUsername = document.querySelector("#username");
  var nodeWork = document.querySelector("#work");
  var nodeSend = document.querySelector("#send");
  var nodeMessage = document.querySelector("#message");
  var nodeTarget = document.querySelector("#target");
  var nodeReceived = document.querySelector("#received");
  var nodeSent = document.querySelector("#sent");
  var nodePull = document.querySelector("#pull");
  var nodePullSent = document.querySelector("#pullSent");
  var nodeSubscribe = document.querySelector("#subscribe");
  var nodeTargetSub = document.querySelector("#targetSub");

  nodeWork.classList.add("hidden");
  nodeErrors.classList.add("hidden");

  nodeErrorClear.addEventListener("click", function () {
    nodeErrors.classList.add("hidden");
  });

  nodeCreate.addEventListener("submit", function (e) {
    e.preventDefault();
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/users/" + nodeUsername.value);
    xhr.onreadystatechange = function () {
      if (xhr.readyState == 4) {
        if (xhr.status == 200) {
          nodeInit.classList.add("hidden");
          nodeWork.classList.remove("hidden");
        } else if (xhr.status == 400) {
          showError(xhr.responseText);
          nodeInit.classList.add("hidden");
          nodeWork.classList.remove("hidden");
        } else {
          showError(xhr.responseText);
        }
      }
    };

    xhr.send();
  });

  nodeSend.addEventListener("submit", function (e) {
    e.preventDefault();
    var url = nodeTarget.value && nodeTarget.value.length > 0
      ? "/" + nodeUsername.value + "/send/" + nodeTarget.value
      : "/" + nodeUsername.value + "/send/";
    var xhr = new XMLHttpRequest();
    xhr.open("POST", url);
    xhr.send(nodeMessage.value);
  });

  nodePull.addEventListener("click", function (e) {
    e.preventDefault();
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/" + nodeUsername.value + "/pull");
    xhr.onreadystatechange = function () {
      if (xhr.readyState == 4) {
        if (xhr.status == 200) {
          nodeReceived.innerHTML = "<div>" + xhr.responseText + "</div>";
        } else {
          showError(xhr.responseText);
        }
      }
    };

    xhr.send();
  });

  nodePullSent.addEventListener("click", function (e) {
    e.preventDefault();
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/" + nodeUsername.value + "/sent");
    xhr.onreadystatechange = function () {
      if (xhr.readyState == 4) {
        if (xhr.status == 200) {
          nodeSent.innerHTML = "<div>" + xhr.responseText + "</div>";
        } else {
          showError(xhr.responseText);
        }
      }
    };

    xhr.send();
  });

  nodeSubscribe.addEventListener("submit", function (e) {
    e.preventDefault();
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/" + nodeUsername.value + "/subscribe/" + nodeTargetSub.value);
    xhr.onreadystatechange = function () {
      if (xhr.readyState == 4) {
        if (xhr.status == 200) {
          // do nothing
        } else {
          showError(xhr.responseText);
        }
      }
    };

    xhr.send();
  });

  function showError(error) {
    nodeErrorContent.textContent = error;
    nodeErrors.classList.remove("hidden");
  }

  var waiting = 5000;

  function fetch() {
    if (!nodeWork.classList.contains("hidden")) {
      var xhr = new XMLHttpRequest();
      xhr.open("GET", "/" + nodeUsername.value + "/fetch");
      xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
          if (xhr.status == 200) {
            nodeReceived.innerHTML += "<div>" + xhr.responseText + "</div>";
          } else {
            nodeErrorContent.textContent = xhr.responseText;
          }
        }
      };

      xhr.send();
    }
    setTimeout(fetch, waiting);
  }

  setTimeout(fetch, waiting);
})();