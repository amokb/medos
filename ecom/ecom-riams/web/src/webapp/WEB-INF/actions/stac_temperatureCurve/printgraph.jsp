<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.nuzmsh.ru/tags/msh" prefix="msh" %>
<%@ taglib uri="http://www.ecom-ast.ru/tags/ecom" prefix="ecom" %>
<tiles:insert page="/WEB-INF/tiles/printGraphLayouts.jsp" flush="true">

    <tiles:put name='title' type='string'>
        <ecom:titleTrail beginForm="mis_medCaseForm" mainMenu="Patient" title="Температурные листы"/>
        <div class='titleTrail'>
            <span> Температурный лист </span>
        </div>
    </tiles:put>

    <tiles:put name='body' type='string'>
        <div id="plot" style="width:90%;height:600px"> &mdash;</div>

        <p id="choices">Показать:</p>


    </tiles:put>

    <tiles:put name='javascript' type='string'>

        <!--[if IE]><script type="text/javascript" src="/skin/ext/flot/js/excanvas.js"></script><![endif]-->
        <script type="text/javascript" src="/skin/ext/flot/js/jquery.js"></script>
        <script type="text/javascript" src="/skin/ext/flot/js/jquery.flot.js"></script>
        <script type='text/javascript' src='./dwr/interface/HospitalMedCaseService.js'></script>

        <script language="javascript" type="text/javascript">

            var jsonar;

            HospitalMedCaseService.getListTemperatureCurve(
                '${param.id}'
                , {
                    callback: function (aString) {

                        var d_degree = new Array();
                        var d_respirationRate = new Array();
                        var minDate, maxDate;
                        $(function () {
                            var jsonar = eval('(' + aString + ')');
                            var childs = jsonar.childs;
                            for (var i = 0; i < childs.length; i++) {
                                var itemar = childs[i];
                                var dateCurrent = new Date(itemar.date);
                                if (i == 0) {
                                    minDate = dateCurrent.getTime();
                                    maxDate = dateCurrent.getTime();
                                } else {
                                    if (minDate > dateCurrent.getTime()) minDate = dateCurrent.getTime();
                                    if (maxDate < dateCurrent.getTime()) maxDate = dateCurrent.getTime();
                                }
                                d_degree.push([dateCurrent.getTime(), itemar.degree]);
                                d_respirationRate.push([dateCurrent.getTime(), itemar.respirationRate]);

                            }

                            var datasets = [
                                {
                                    label: "Температура",
                                    data: d_degree
                                }
                            ];

                            var options = {
                                lines: {show: true},
                                points: {show: true},
                                legend: {noColumns: 2},
                                xaxis: {
                                    mode: "time",
                                    minTickSize: [1, "hour"],
                                    tickSize: [4, "hour"],
                                    timeformat: "%d д %h ч"
                                    ,
                                    min: minDate,
                                    max: maxDate
                                },
                                selection: {mode: "x"}
                            };


                            var i = 0;
                            $.each(datasets, function (key, val) {
                                val.color = i;
                                ++i;
                            });

                            // insert checkboxes
                            var choiceContainer = $("#choices");
                            $.each(datasets, function (key, val) {
                                choiceContainer.append('<br/><input type="checkbox" name="' + key +
                                    '" checked="checked" >' + val.label + '</input>');
                            });
                            choiceContainer.find("input").click(plotAccordingToChoices);


                            function plotAccordingToChoices() {
                                var data = [];

                                choiceContainer.find("input:checked").each(function () {
                                    var key = $(this).attr("name");
                                    if (key && datasets[key])
                                        data.push(datasets[key]);
                                });

                                if (data.length > 0)
                                    $.plot($("#plot"), data, options);
                            }

                            plotAccordingToChoices();

                        });


                    }
                });
            xaxis: {
                mode: "time"
                timeformat: "%d.%m.%y %H:%M"
            }

        </script>

        <script type="text/javascript">
            function printGraph() {
                document.getElementById('printGraph').style.visibility = 'hidden';
                print();
            }
        </script>

        <input id="printGraph" type=button value="Печатать" onClick="printGraph()">
    </tiles:put>


</tiles:insert>