"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var core_1 = require('@angular/core');
var JsonEditorComponent = (function () {
    function JsonEditorComponent() {
    }
    JsonEditorComponent.prototype.ngAfterViewInit = function () {
        this.editor = new JSONEditor(this.editorElement.nativeElement);
        this.editor.setName(this.name);
    };
    JsonEditorComponent.prototype.getJson = function () {
        return this.editor.get();
    };
    JsonEditorComponent.prototype.setJson = function (json) {
        this.editor.set(json);
    };
    JsonEditorComponent.prototype.setMode = function (mode) {
        this.editor.setMode(mode);
    };
    JsonEditorComponent.prototype.setName = function (name) {
        this.editor.setName(name);
    };
    JsonEditorComponent.prototype.fooFunction = function () {
        console.log("bananer");
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Object)
    ], JsonEditorComponent.prototype, "name", void 0);
    __decorate([
        core_1.ViewChild('editorElement'), 
        __metadata('design:type', Object)
    ], JsonEditorComponent.prototype, "editorElement", void 0);
    JsonEditorComponent = __decorate([
        core_1.Component({
            selector: 'json-editor',
            template: '<div #editorElement style="height:100%;"></div>'
        }), 
        __metadata('design:paramtypes', [])
    ], JsonEditorComponent);
    return JsonEditorComponent;
}());
exports.JsonEditorComponent = JsonEditorComponent;
//# sourceMappingURL=json-editor.component.js.map