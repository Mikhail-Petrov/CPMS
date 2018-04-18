var csrfHeader = $("meta[name='_csrf_header']").attr("content");
var csrfToken = $("meta[name='_csrf']").attr("content");
var headers = {};
headers[csrfHeader] = csrfToken;

$( '#skillSelector' ).on('change', function() {
	var skill = $(this).val()
	var inputArgs = [];
	inputArgs.push(skill);
	if (skill != null && skill > 0) {
		$.ajax({
			url : rootPath + 'viewer/ajaxSkill',
			type: 'POST',
			dataType: 'json',
			contentType: 'application/json; charset=utf-8',
			data : JSON.stringify(inputArgs),
			headers: headers,
			success: function (res) {
				document.getElementById("maxLevel").innerHTML = res.maxLevel;
				$('#levelNumber').attr({
				       "max" : res.maxLevel
				});
			}
		});
	}
});

$( '#skillSelector' ).trigger('change')