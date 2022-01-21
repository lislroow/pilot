function fn_checkKeycode(e) {
	//console.log(e.keyCode);
	return true;
}

function fn_checkRegExpStr(str) {
  str = str.replace(/([\/\.])/, '\\$1');
  //str = str.replace(/([\/])/, '\\$1');
  return str;
}

function fn_getDataAll() {
  fn_getDataTbl01();
  fn_getDataTbl02();
  fn_getDataTbl03();
  fn_getDataTbl04();
  fn_getDataTbl05();
  fn_getDataTbl06();
}

function fn_getDataTbl01(fn_callback) {
  $.ajax({
    url: baseUrl + '/api/com/runtime/spring-beans',
    success: function(data) {
      var list = data.body;
      var tblId = 'tbl01';
      fn_setDataTbl01(tblId, list);
      fn_setEvent(tblId);
    },
    complete: fn_callback == null ? fn_getDataTbl01_callback : fn_callback
  });
}

function fn_getDataTbl01_callback() {
  var tblId = 'tbl01';
  var $tr_list = $('#'+tblId+' > tbody > tr');
  $tr_list.hide();
}

function fn_getDataTbl02(fn_callback) {
  $.ajax({
    url: baseUrl + '/api/com/runtime/spring-uri',
    success: function(data) {
      var list = data.body;
      var tblId = 'tbl02';
      fn_setDataTbl02(tblId, list);
      fn_setEvent(tblId);
    },
    complete: fn_callback == null ? fn_getDataTbl02_callback : fn_callback
  });
}

function fn_getDataTbl02_callback() {
  var tblId = 'tbl02';
  var $tr_list = $('#'+tblId+' > tbody > tr');
  $tr_list.hide();
}

function fn_getDataTbl03(fn_callback) {
  $.ajax({
    url: baseUrl + '/api/com/runtime/spring-security-uri',
    success: function(data) {
      var list = data.body;
      var tblId = 'tbl03';
      fn_setDataTbl03(tblId, list);
      fn_setEvent(tblId);
    },
    complete: fn_callback == null ? fn_getDataTbl03_callback : fn_callback
  });
}

function fn_getDataTbl03_callback() {
  var tblId = 'tbl03';
  var $tr_list = $('#'+tblId+' > tbody > tr');
  $tr_list.hide();
}

function fn_getDataTbl04(fn_callback) {
  $.ajax({
    url: baseUrl + '/api/com/runtime/mybatis-mapper',
    success: function(data) {
      var list = data.body;
      var tblId = 'tbl04';
      fn_setDataTbl04(tblId, list);
      fn_setEvent(tblId);
    },
    complete: fn_callback == null ? fn_getDataTbl04_callback : fn_callback
  });
}

function fn_getDataTbl04_callback() {
  var tblId = 'tbl04';
  var $tr_list = $('#'+tblId+' > tbody > tr');
  $tr_list.hide();
}

function fn_getDataTbl05(fn_callback) {
  $.ajax({
    url: baseUrl + '/api/com/runtime/properties',
    success: function(data) {
      var list = data.body;
      var tblId = 'tbl05';
      fn_setDataTbl05(tblId, list);
      fn_setEvent(tblId);
    },
    complete: fn_callback == null ? fn_getDataTbl05_callback : fn_callback
  });
}

function fn_getDataTbl05_callback() {
  var tblId = 'tbl05';
  var $tr_list = $('#'+tblId+' > tbody > tr');
  $tr_list.hide();
}

function fn_getDataTbl06(fn_callback) {
  $.ajax({
    url: baseUrl + '/api/com/runtime/jdbc-datasource',
    success: function(data) {
      var list = data.body;
      var tblId = 'tbl06';
      fn_setDataTbl06(tblId, list);
      fn_setEvent(tblId);
    },
    complete: fn_callback == null ? fn_getDataTbl06_callback : fn_callback
  });
}

function fn_getDataTbl06_callback() {
  var tblId = 'tbl06';
  var $tr_list = $('#'+tblId+' > tbody > tr');
  $tr_list.hide();
}

function fn_setEvent(tblId) {
  $('#'+tblId+' > tbody').find('tr').on('dblclick', function() { $(this).toggleClass('checked'); });
  $('#'+tblId+' > tbody').find('tr').on('mouseout', function() { $(this).removeClass('selected'); });
  $('#'+tblId+' > tbody').find('tr').on('mouseover', function() { $(this).addClass('selected'); });
}

function fn_filterToggle(tableId, filterType) {
  var $tr_list = $('#'+tableId+' > tbody > tr');
  if($('#'+tableId+'_'+'filter'+filterType+'_chk'+'_ALL').prop('checked')) {
    $tr_list.show();
    $('[id^='+tableId+'_'+'filter'+filterType+'_chk'+']').prop('checked', true);
  } else {
    $tr_list.hide();
    $('[id^='+tableId+'_'+'filter'+filterType+'_chk'+']').prop('checked', false);
  }
  fn_numberingTbl(tableId);
}

function fn_numberingTbl(tableId) {
  var $tr_list = $('#'+tableId+' > tbody > tr:visible');
  for(var i=0; i<$tr_list.length; i++) {
    $tr_list.eq(i).find('td:nth-child(1)').text(($tr_list.length-i)).attr('data', ($tr_list.length-i));
  }
}