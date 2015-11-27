;
(function (jq) {
	jq(function () {
		jq('#btnAnalyze').click(function () {
			displayAnalyzeDialog();
		});
	});
	this.displayAnalyzeDialog = function () {
		var localPackageItems = jq('.localPackage');
		var data = {};
		localPackageItems.each(function (idx, localStackItem) {
			var localTid = jq(localStackItem).attr("tid");
			var traceLine = jq("span", localStackItem).text();
			var lineInfo = data[traceLine];
			if (!lineInfo) {
				data[traceLine] = lineInfo = {};
				lineInfo.count = 0;
			}
			//count line only once for each different thread
			if (lineInfo.tid != localTid) {
				lineInfo.count += 1;
				lineInfo.tid = localTid;
			}
			//lineInfo.blocked += ()
		});
		var reportHolder = jq('#analyzeModal .modal-body');
		reportHolder.empty();
		var sortable = [];
		for (var stackLine in data) {
			if (data[stackLine].count > 1) {
				sortable.push([stackLine, data[stackLine].count])
			}
		}
		sortable.sort(function (a, b) {
			return b[1] - a[1]
		})
		if (sortable.length == 0) {
			jq('<span>').text("Nothing found")
		}
		var row = jq("<div>", {
			"class": "row",
		}).appendTo(reportHolder).append(
				jq("<div>", {
					"class" : "col-md-10",
					"text" : "Stack trace element"
				})
		).append(
				jq("<div>", {
					"class": "col-md-2",
					"text" : "# threads"
				})
		);
		sortable.forEach(function(reportLine, idx) {
			var row = jq("<div>", {
				"class": "row"
			}).appendTo(reportHolder);
			var leftCol = jq("<div>", {
				"class": "col-md-10"
			}).appendTo(row);
			var rightCol = jq("<div>", {
				"class": "col-md-2"
			}).appendTo(row);
			jq("<span>", {
				text: reportLine[0]
			}).appendTo(leftCol);
			jq("<span>", {
				text: reportLine[1]
			}).appendTo(rightCol);
		});
		jq('#analyzeModal').modal({});
	}
})($);