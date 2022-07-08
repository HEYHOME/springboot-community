$(function () {
    $("#sendBtn").click(send_letter);
    $(".close").click(delete_msg);
});

function send_letter() {
    $("#sendModal").modal("hide");

    let toName = $("#recipient-name").val();
    let content = $("#message-text").val();
    $.ajax({
        type: 'post',
        url: CONTEXT_PATH + '/letter/send',
        dataType: 'json',
        data: {toName: toName, content: content},
        success: function (data) {
            if (data.code==0){
            	$("#hintBody").text("发送成功！");
			}else{
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function () {
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
        }
    })


}

function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}