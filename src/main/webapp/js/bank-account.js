$(document).ready(function () {
    var table = $('#accounts').DataTable({
        "processing": true,
        "serverSide": true,
        "ajax": {
            "url": "/bankAccount/list",
            "type": "GET"
        },
        "columns": [
            {"data": "id", "defaultContent": ""},
            {"data": "number", "defaultContent": ""},
            {"data": "userName", "defaultContent": ""},
            {
                "data": function (data) {
                    return '<input id="' + data.id + '" type="button" class="payment" value="Получить баланс" onclick="getBalance(this);">';
                }, "defaultContent": ""
            },
            {
                "data": function (data) {
                    return '<input id="' + data.id + '" type="button" class="delete" value="Удалить счет" onclick="deleteAccount(this);">';
                }, "defaultContent": ""
            },
        ]
    });

    $('#addRow').on('click', function () {
        if ($("#user").val().length > 0 && $("#number").val().length > 0) {
            $.ajax({
                type: "POST",
                url: "/bankAccount/save",
                data: JSON.stringify({
                    userName: $("#user").val(),
                    number: $("#number").val()
                }),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    table.row.add([
                        data,
                        $("#number").val(),
                        $("#user").val(),
                        '<input id="' + data + '" type="button" class="payment" value="Получить баланс" onclick="getBalance(this);">',
                        '<input id="' + data + '" type="button" class="delete" value="Удалить счет" onclick="deleteAccount(this);">',
                    ]).draw();
                    $("#number").val("");
                    $("#user").val("");
                }
            });
        } else alert("Укажите данные счета");
    });

    $('#transfer').on('click', function () {
        if ($("#to").val().length !== 0 && $("#amount").val().length !== 0) {
            $.ajax({
                type: "POST",
                url: "/payment/transfer",
                data: {
                    from: $("#from").val(),
                    to: $("#to").val(),
                    amount: $("#amount").val()
                },
                success: function (data) {
                    $("#from").val("");
                    $("#to").val("");
                    $("#amount").val("");
                    $("#error").text("");
                },
                error: function (data, textStatus, xhr) {
                    $("#error").text(data.responseText);
                }
            });
        } else alert("Укажите данные для перевода");
    });

    $('#accounts tbody').on('click', 'tr', function () {
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
        }
        else {
            table.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
        }
    });
})
;

function deleteAccount(obj) {
    $.ajax({
        type: "DELETE",
        url: "/bankAccount/delete/" + $(obj).attr("id"),
        success: function (data) {
            $(obj).closest('tr').remove();
        }
    });
}

function getBalance(obj) {
    $.ajax({
        type: "GET",
        url: "/payment/balance/" + $(obj).attr("id"),
        success: function (data) {
            if ($(obj).parent().find("strong").length === 0) {
                $(obj).parent().append("<strong>" + $(data).text() + "</strong>");
            }
            $(obj).parent().find("strong").replaceWith("<strong>" + $(data).text() + "</strong>");
        }
    });
}