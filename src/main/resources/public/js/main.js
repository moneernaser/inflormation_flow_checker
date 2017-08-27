// grab your file object from a file input
$('#fileUpload').change(onChangeFile);

function onChangeFile() {
    sendFile(this.files[0]);
    $(this).remove();
    $("<input type='file' id='fileUpload' style='display: none;'>").change(onChangeFile).appendTo($(".inputWrapper"));
}

function sendFile(file) {


    var file_data = file;
    var form_data = new FormData();
    form_data.append("file", file_data)
    $.ajax({
        url: "/api/upload",
        dataType: 'json',
        cache: false,
        contentType: false,
        processData: false,
        data: form_data,
        type: 'post'
    })
    .done(function( data ) {
            $("#variables_list").load('/api/variables/all', function () {
                $('#variables_modal').modal('show');
                window.variables = data.variablesList;
                console.log( "data: " + JSON.stringify(data) );
            });

        });
}

$('#testExample').click(function(){
    $.ajax({
        url: "/api/example/BankAccount",
        dataType: 'json',
        cache: false,
        contentType: false,
        processData: false,
        type: 'get'
    })
        .done(function( data ) {
            $("#variables_list").load('/api/variables/all', function () {
                $('#variables_modal').modal('show');
                window.variables = data.variablesList;
                console.log( "data: " + JSON.stringify(data) );
            });

        });
});

window.submitVariables = function () {
    for (var i in window.variables) {
        var level = $('#variable_' + i).val();
        window.variables[i]['securityLevel'] = level;
            //reset display
            $('#variable_identifier_'+i).css("color", "black");
            $('#variable_identifier_'+i + " .glyphicon-arrow-left").css("display", "none");
            $('#variable_identifier_'+i + " .glyphicon-arrow-right").css("display", "none");
    }
    $.ajax({
        url: "/api/variables_levels",
        dataType: 'json',
        contentType : 'application/json',
        data: JSON.stringify({vars: window.variables}),
        type: 'post'
    })
        .done(function( data ) {
            window.processResults(data)
        });
};

window.processResults = function (data) {

    if (isEmpty(data)) {
        alert("code is secure!");
    } else {
        for (var ei in data) {
            for (var vi1 in window.variables) {
                var v = window.variables[vi1];
                var resVarKey = data[ei].key;
                if (v['identifier'] === resVarKey['identifier']
                    && v['varType'] === resVarKey['varType']
                    && v['lineDefined'] === resVarKey['lineDefined']) {
                    $('#variable_identifier_'+vi1).css("color", "red");
                    $('#variable_identifier_'+vi1 + " .glyphicon-arrow-left").css("display", "inline-block");
                }
            }
            for (var vi2 in window.variables) {
                var v = window.variables[vi2];
                var resVarKeyValues = data[ei].values;
                for (var rvi in resVarKeyValues) {
                    var rv = resVarKeyValues[rvi];
                    if (v['identifier'] === rv['identifier']
                        && v['varType'] === rv['varType']
                        && v['lineDefined'] === rv['lineDefined']) {
                        $('#variable_identifier_'+vi2).css("color", "red");
                        $('#variable_identifier_'+vi2 + " .glyphicon-arrow-right").css("display", "inline-block");
                    }
                }

            }
        }
    }
};

function isEmpty(obj) {
    for(var key in obj) {
        if(obj.hasOwnProperty(key))
            return false;
    }
    return true;
}

