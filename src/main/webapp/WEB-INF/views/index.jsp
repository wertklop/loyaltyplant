<%@include file="header.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<h2>Таблица банковских счетов</h2>
<label>Номер счета</label>
<input type="text" id="number">
<label>Пользователь счета</label>
<input type="text" id="user">
<input type="button" id="addRow" value="Добавить счет">

<table id="accounts" class="dataTable" cellpadding="0">
    <thead>
    <tr>
        <th>Id счета</th>
        <th>Номер счета</th>
        <th>Пользователь счета</th>
        <th></th>
        <th></th>
    </tr>
    </thead>
</table>

<div>
    <label>Счет-отправитель(Id)</label>
    <input type="text" id="from">
    <label>Счет-получатель(Id)</label>
    <input type="text" id="to">
    <label>Сумма перевода</label>
    <input type="text" id="amount">
    <input type="button" id="transfer" value="Перевести сумму">
</div>
<p id="error" style="color: red;"></p>

<%@include file="footer.jsp" %>