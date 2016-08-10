import { WidgetKind } from './widget-kind';
import { Faces } from './erica-components/faces.component';
import { AwesomeNG2Component } from './lazy-widgets/ng2Inside/awesome-ng2-component.component';
import { MapComponent } from './erica-components/map.component';
import { changeTable } from './erica-components/changeTable.component';
import { barchart_coordinatorComponent } from './erica-components/barchart_coordinator.component';
import { barchart_medicinComponent } from './erica-components/barchart_medicin.component';
import {
    ItemEditorComponent,
    ItemExplorerComponent,
    SOPMakerComponent,
    TrajectoriesComponent,
    OPCRunnerComponent,
    ProcessSimulateComponent,
    ConditionEditorComponent,
    OperationControlComponent,
    KubInputGUIComponent,
    OperatorInstGUIComponent,
    RobotCycleAnalysisComponent,
    ActiveOrderComponent,
    Tobbe2Component
} from './upg-helpers/upg-ng1-widget-containers';

export const widgetKinds: WidgetKind[] = [
    { 'component': Faces, 'title': 'ERICA Faces', 'sizex': 4, 'sizey': 4, 'id': null, 'gridOptions': null },
    { 'component': AwesomeNG2Component, 'title': 'ng2Inside', 'sizex': 4, 'sizey': 4, 'id': null, 'gridOptions': null },
    { 'component': MapComponent, 'title': 'ERICA Map', 'sizex': 4, 'sizey': 4, 'id': null, 'gridOptions': null },
    { 'component': changeTable, 'title': 'ERICA Change Table', 'sizex': 4, 'sizey': 4, 'id': null, 'gridOptions': null },
    { 'component': barchart_coordinatorComponent, 'title': 'ERICA Bars Coord', 'sizex': 4, 'sizey': 4, 'id': null, 'gridOptions': null },
    { 'component': barchart_medicinComponent, 'title': 'ERICA Bars Med', 'sizex': 4, 'sizey': 4, 'id': null, 'gridOptions': null },
    { 'component': KubInputGUIComponent, 'title': 'upg-kub', 'sizex': 4, 'sizey': 4, 'id': null, 'gridOptions': null }
]
