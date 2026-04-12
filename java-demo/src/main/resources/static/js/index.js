var layer;
$(function(){
    layer=layui.use('layer');
});

function adduser(){
    layer.open({
          id :"addUser",
          title :"添加用户",
          type: 2,
          content: 'addUserPage', //这里content是一个URL，如果你不想让iframe出现滚动条，你还可以content: ['${contextPath}/s/loadProdListPage', 'no']
          area: ['450px', '350px']
        });
}

function queryuserList(){
    layer.open({
          title :"用户列表",
          type: 2,
          content: 'queryUserListPage', //这里content是一个URL，如果你不想让iframe出现滚动条，你还可以content: ['${contextPath}/s/loadProdListPage', 'no']
          area: ['900px', '650px']
        });
}
