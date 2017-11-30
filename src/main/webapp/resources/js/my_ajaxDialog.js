/**
 * An AJAX script to remember profile
 */
function my_ajaxDialog(requestUrl) {
	
	var inputArgs = [];
	var i;
	for (i = 1; i < arguments.length; i++) {
		inputArgs.push(arguments[i]);
	}
	
	 var csrfHeader = $("meta[name='_csrf_header']").attr("content");
     var csrfToken = $("meta[name='_csrf']").attr("content");
     var headers = {};
     headers[csrfHeader] = csrfToken;
	
	$.ajax({
		url : requestUrl,
		type: 'POST',
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		data : JSON.stringify(inputArgs),
		headers: headers,
		success: function (res) {
			document.getElementById('notificationBody').innerHTML=res.message
			$('#notificationModal').modal('show'); 
		},
		error: function (xhRequest, ErrorText, thrownError) {
			console.log('xhRequest: ' + xhRequest + "\n");
			console.log('ErrorText: ' + ErrorText + "\n");
			console.log('thrownError: ' + thrownError + "\n");
			console.log('JSON request: ' + JSON.stringify(inputArgs) + "\n");
		}
	});
}