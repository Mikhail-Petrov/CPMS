function addCheck(element, min, max) {
	var len = element.value.length;
	if (len < min || len > max)
		element.style.borderColor = 'red';
	else
		element.style.borderColor = '#d7d7d7';
}