$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');
            } else {
                alert(data.msg);
            }
        }
    )
}

// 置顶
function setTop() {
    $.ajax({
        type: "post",
        url: CONTEXT_PATH + "/discuss/top",
        dataType: "json",
        data: {"id": $("#postId").val()},
        success: function (data) {
            if (data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    })
}

// 加精
function setWonderful() {
    $.ajax({
        type: "post",
        url: CONTEXT_PATH + "/discuss/wonderful",
        dataType: "json",
        data: {"id": $("#postId").val()},
        success: function (data) {
            if (data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    })
}

// 删除
function setDelete() {
    $.ajax({
        type: "post",
        url: CONTEXT_PATH + "/discuss/delete",
        dataType: "json",
        data: {"id": $("#postId").val()},
        success: function (data) {
            if (data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    })
}
