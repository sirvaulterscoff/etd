;
var localPackageNames = [];
var libs = {};
var colorMap = {};
var colors = 3;
(function (jq) {
	function RestClient() {
		var that = this;

		function doRestPut(group, mtd, data) {
			var _url = "rest/" + group + "/" + mtd + "/" + data;
			jq.ajax({
				url: _url,
				method: 'PUT'
			}).then(function (data) {
				if (data["err"]) {
					alert(data["err"]);
				}
			});
		}

		this.onFillVmInfoData = function (data) {
			if (data["err"]) {
				alert(data["err"]);
				return;
			}
			var chkMonitorInfo = jq('#chkMonitorInfo');
			chkMonitorInfo.prop("disabled", data["monitorUsageSupported"] == "false");
			chkMonitorInfo.change(function () {
				doRestPut("vmInfo", "monitorsEnabled", jq(this).prop("checked"))
			});

			jq('#chkSynchInfo').prop("checked", data["synchSuported"] == "true");

			var chkContention = jq('#chkContention');
			chkContention.prop("disabled", data["contentionSupported"] == "false");
			chkContention.prop("checked", data["contentionEnabled"] == "true");
			chkContention.change(function () {
				doRestPut("vmInfo", "contentionEnabled", jq(this).prop("checked"))
			});

			var chkCpuTime = jq('#chkCpuTime');
			chkCpuTime.prop("disabled", data["cpuSupported"] == "false");
			chkCpuTime.prop("checked", data["cpuEnabled"] == "true");
			chkCpuTime.change(function () {
				doRestPut("vmInfo", "cpuEnabled", jq(this).prop("checked"))
			});
		};

		this.parseThreadLine = function (thread) {
			var originalHeader = jq("#thread-template");
			var clonedRow = originalHeader.clone(true);
			clonedRow.insertAfter(originalHeader);
			clonedRow.attr("tid", thread.header.id);
			clonedRow.prop("id", "thread-" + thread.header.id);
			clonedRow.show();

			var state = jq(".tstate", clonedRow);
			//noinspection FallThroughInSwitchStatementJS
			state.prop('title', thread.header.state);
			switch (thread.header.state) {
				case "RUNNABLE":
					state.text("R");
					state.addClass("label-success").addClass("label");
					break;
				case "BLOCKED":
					state.text("B");
					state.addClass("label label-danger");
					break;
				case "WAITING":
					state.text("W");
					state.addClass("label label-warning");
					break;
				case "TIMED_WAITING":
					state.text("T");
					state.addClass("label label-warning");
					break;
				default:
					state.text(thread.header.state);
			}

			var aTag = jq(".tname", clonedRow);
			aTag.text(thread.header.name);
			aTag.prop("href", "#" + thread.header.id);

			var extInfo = String.format("id: {0}, block: cnt {1} time {2}, wait: cnt {3} time {4}, cpu: {5} user {6}", thread.header.id,
					thread.counts.block, thread.times.block,
					thread.counts.wait, thread.times.wait,
					thread.times.cpu, thread.times.userCpu);
			jq(".tid", clonedRow).text(extInfo);

			var previousPackage;
			var groupRow;
			var afterGroupTag = false;
			thread.stack.reverse().forEach(function (stackElem) {
				var rootRowTag = jq("<div>", {
					class: "row trace-elem"
				});
				var holder = jq('<div>', {
					class: "col-md-8"
				}).appendTo(rootRowTag);
				jq("<span>", {
					text: stackElem.line
				}).appendTo(holder);
				var packageMatched = false;
				if (localPackageNames.some(that.packageMatches(stackElem.line))) {
					holder.addClass("localPackage");
					packageMatched = true;
				} else {
					for (var key in libs) {
						if (libs.hasOwnProperty(key)) {
							if (that.packageMatches(stackElem.line)(key)) {
								var currentPackage = libs[key];
								var colorNum = colorMap[currentPackage];
								var colorCssClass = "pcolor_" + colorNum % colors;
								holder.addClass(colorCssClass);
								packageMatched = true;
								rootRowTag.attr("pkgid", colorNum);
								if (!previousPackage || previousPackage != currentPackage) {
									previousPackage = currentPackage;
									//that.createStub()
									var stubHeader = jq("<div>", {
										class: "row package-stub-header " + colorCssClass
									});
									stubHeader.attr("pkgid", colorNum);
									var stubBodyHolder = jq('<div>', {
										class: 'col-md-8 package-stub-x'
									}).appendTo(stubHeader);
									jq('<span>', {
										text: currentPackage
									}).appendTo(stubBodyHolder);
									stubHeader.insertAfter(clonedRow);
									groupRow = stubHeader;
								}
							}
						}
					}

				}
				if (!packageMatched) {
					holder.addClass("packageUnmatched");
					groupRow = null;
					previousPackage = null;
					rootRowTag.attr("pkgid", -1);
				}
				rootRowTag.insertAfter(groupRow ? groupRow : clonedRow);
			});
		};
		this.packageMatches = function (line) {
			return function (e) {
				return line.lastIndexOf(e, 0) == 0 || new RegExp(e).test(line);
			}
		};

		this.parseStacks = function (data) {
			if (data["err"]) {
				alert(data["err"]);
				return;
			}
			var info = data["info"];
			jq("#divTime").text(info["createdOn"]);
			jq("#divStarted").text(info["started"]);
			jq("#divDaemon").text(info["daemon"]);
			jq("#divPeak").text(info["peak"]);
			var totalThreads = info["count"];
			jq("#divCurrent").text(totalThreads);
			jq("#divRunnable").text(info.runnable + " (" + percent(totalThreads, info.runnable) + ")");
			jq("#divBlocked").text(info.blocked + " (" + percent(totalThreads, info.blocked) + ")");
			jq("#divWaiting").text(info.waiting + " (" + percent(totalThreads, info.waiting) + ")");
			data["threads"].forEach(that.parseThreadLine);
		};

		this.parsePrefs = function (data) {
			if (data["err"]) {
				alert(data["err"]);
				return;
			}
			localPackageNames = data.local;
			var packageTag = jq('#libs_packages');
			libs = {};
			//iterate over libs and for reverse map
			for (var pName in data.libs) {
				//create reverse map
				if (!data.libs.hasOwnProperty(pName)) {
					continue;
				}
				var pList = data.libs[pName];
				pList.forEach(function (pItem) {
					libs[pItem] = pName;
				});

				//create filter holders
				var holder = jq('<label>', {
					class: "btn btn-primary"
				});
				var checkBox = jq('<input>', {
					type: "checkbox",
					id: pName
				});
				checkBox.appendTo(holder);
				holder.append(pName);
				holder.appendTo(packageTag);
				var colorNum;
				var size = Object.size(colorMap);
				colorMap[pName] = colorNum = size;
				holder.addClass("pcolor_" + colorNum % colors);
				checkBox.attr("pkgid", colorNum);
				checkBox.change(function () {
					var $this = jq(this);
					var colorNum = $this.attr("pkgid");
					if ($this.prop("checked")) {
						jq(".trace-elem[pkgid='" + colorNum + "']").hide();
						jq(".package-stub-header[pkgid='" + colorNum + "'").show();
					} else {
						jq(".trace-elem[pkgid='" + colorNum + "']").show();
						jq(".package-stub-header[pkgid='" + colorNum + "'").hide();
					}
				});
			}
		}
	}

	RestClient.prototype.fillVmInfo = function () {
		jq.ajax({
			url: "rest/vmInfo"
		}).then(this.onFillVmInfoData);
	};
	RestClient.prototype.getStacks = function () {
		jq.ajax({
			url: "rest/stacks"
		}).then(this.parseStacks);
	};
	RestClient.prototype.getPreferences = function () {
		jq.ajax({
			url: "rest/prefs"
		}).then(this.parsePrefs);
	};

	jq(function () {
		//get VM info
		var restClient = new RestClient();
		restClient.getPreferences();
		restClient.fillVmInfo();
		//get thread stacks
		restClient.getStacks();

	});
	function percent(all, part) {
		return (part / all * 100).toPrecision(3) + "%";
	}
})($);

String.format = function () {
	// The string containing the format items (e.g. "{0}")
	// will and always has to be the first argument.
	var theString = arguments[0];

	// start with the second argument (i = 1)
	for (var i = 1; i < arguments.length; i++) {
		// "gm" = RegEx options for Global search (more than one instance)
		// and for Multiline search
		var regEx = new RegExp("\\{" + (i - 1) + "\\}", "gm");
		theString = theString.replace(regEx, arguments[i]);
	}

	return theString;
};

Object.size = function (obj) {
	var size = 0, key;
	for (key in obj) {
		if (obj.hasOwnProperty(key)) size++;
	}
	return size;
};