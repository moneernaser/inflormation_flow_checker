function testAPI() {
    console.log("js is live")
    $.post("/api/upload", function (data) {
        console.log(JSON.stringify(data));
    });
}

// grab your file object from a file input
$('#fileUpload').change(function () {
    sendFile(this.files[0]);
});

// can also be from a drag-from-desktop drop
$('dropZone')[0].ondrop = function (e) {
    e.preventDefault();
    sendFile(e.dataTransfer.files[0]);
};

function sendFile(file) {


    var file_data = file;
    var form_data = new FormData();                  // Creating object of FormData class
    form_data.append("file", file_data)              // Appending parameter named file with properties of file_field to form_data
    form_data.append("user_id", 123)                 // Adding extra parameters to form_data
    $.ajax({
        url: "/api/upload2",
        dataType: 'json',
        cache: false,
        contentType: false,
        processData: false,
        data: form_data,                         // Setting the data attribute of ajax with file_data
        type: 'post'
    })
    .done(function( data ) {
        // $("#variables_list").load("/templates/variables.html");
            $("#variables_list").load('/api/variables/all2', function () {
                $('#variables_modal').modal('show');
                window.variables = data.variablesList;
                console.log( "data: " + JSON.stringify(data) );
            });

        });

window.submitVariables = function () {
    for (var i in window.variables) {
        var level = $('#variable_' + i).val();
        window.variables[i]['securityLevel'] = level;
    }
    // var form_data = new FormData();                  // Creating object of FormData class
    // form_data.append("vars", window.variables)              // Appending parameter named file with properties of file_field to form_data
    $.ajax({
        url: "/api/variables_levels",
        dataType: 'json',
        contentType : 'application/json',
        data: JSON.stringify({vars: window.variables}),                         // Setting the data attribute of ajax with file_data
        type: 'post'
    })
        .done(function( data ) {
            // $("#variables_list").load("/templates/variables.html");
            $("#variables_list").load('/api/variables/all2', function () {
                $('#variables_modal').modal('show');
                console.log( "data: " + JSON.stringify(data) );
            });

        });
}
//show popup with variable and submit butom




    // $.ajax({
    //     type: 'post',
    //     url: '/api/upload',
    //     data: {file: file},
    //     success: function (data) {
    //         console.log("file uploaded. data: " + JSON.stringify(data))
    //     },
    //     xhrFields: {
    //         // add listener to XMLHTTPRequest object directly for progress (jquery doesn't have this yet)
    //         onprogress: function (progress) {
    //             // calculate upload progress
    //             var percentage = Math.floor((progress.total / progress.totalSize) * 100);
    //             // log upload progress to console
    //             console.log('progress', percentage);
    //             if (percentage === 100) {
    //                 console.log('DONE!');
    //             }
    //         }
    //     },
    //     processData: false,
    //     contentType: file.type
    // });
}