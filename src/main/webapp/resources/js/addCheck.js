function addCheck(element, min, max) {
	var len = element.value.length;
	if (len < min || len > max && max > 0)
		element.style.borderColor = 'red';
	else
		element.style.borderColor = '#d7d7d7';
}
function addCheck(element, min, max, warning) {
	var len = element.value.length;
	if (len < min || len > max && max > 0) {
		element.style.borderColor = 'red';
		$("#warningText").html(warning);
	} else {
		element.style.borderColor = '#d7d7d7';
		$("#warningText").html();
	}
}