var tableChangePage = function(page, table, loadUrl) {
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var headers = {};
    headers[csrfHeader] = csrfToken;
                       				
    var inputArgs = [];
    inputArgs.push(page);
                       			     
    $.ajax({
        url : loadUrl,
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        data : JSON.stringify(inputArgs),
        headers: headers,
        success: function (res) {
			table.bootstrapTable('load', res);
        },
        error: function (xhRequest, ErrorText, thrownError) {
			console.log('xhRequest: ' + xhRequest + "\n");
            console.log('ErrorText: ' + ErrorText + "\n");
            console.log('thrownError: ' + thrownError + "\n");
            console.log('JSON request: ' + JSON.stringify(inputArgs) + "\n");
        }
    });
};