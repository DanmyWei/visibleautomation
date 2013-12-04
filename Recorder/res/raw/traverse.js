
function overrideclicks() {
    traverse(document.documentElement);
}

function isClickable(element) {
    return (element.nodeType == 1) && 
    		(element.hasAttribute('onclick') || 
    		 ((element.nodeName.toLowerCase() == 'input') && (element.getAttribute('type').toLowerCase() == 'submit')));
}

function traverse(element) {
	if (isClickable(element)) {
		injectedObject.recordEvent(element.nodeName, 'assign');
        element.addEventListener('click', overClick);
	}
    for (var i = 0; i < element.childNodes.length; i++) {
       traverse(element.childNodes.item(i));
    }
}

function getRelativeReference(el, parent) {
    for (var i = 0; i < parent.childNodes.length; i++) {
        var childNode = parent.childNodes.item(i);
        if (childNode.isSameNode(el)) {
            return childNode.nodeName + '[' + i + ']';
        }
    }
}

function getReference(element) {
    var reference = '';
    var start = true;
    while (element != document.documentElement) {
        var parent = element.parentNode;
        var ref = getRelativeReference(element, parent);
        if (start) {
            reference = ref;
            start = false;
        } else {
                reference = ref + '.' + reference;
        }
        element = parent;
    }
    return reference;
}


function overClick() {
	injectedObject.recordEvent(getReference(event.target), 'click');
}

function overrideclicks() {
	injectedObject.recordEvent('override', 'assign');
        traverse(document.documentElement);
}

