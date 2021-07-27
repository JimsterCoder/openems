import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { Service, Utils } from 'src/app/shared/shared';
import { InstallationData } from '../../installation.component';

export type AcPv = {
  alias: string,
  value: number,
  orientation: string,
  moduleType: string,
  modulesPerString: number,
  meterType: string,
  modbusCommunicationAddress: number
}

@Component({
  selector: ProtocolAdditionalAcProducersComponent.SELECTOR,
  templateUrl: './protocol-additional-ac-producers.component.html'
})
export class ProtocolAdditionalAcProducersComponent implements OnInit {

  private static readonly SELECTOR = "protocol-additional-ac-producers";

  @Input() public installationData: InstallationData;

  @Output() public previousViewEvent: EventEmitter<any> = new EventEmitter();
  @Output() public nextViewEvent = new EventEmitter<InstallationData>();

  public form: FormGroup;
  public fields: FormlyFieldConfig[];
  public model;

  public insertModeEnabled: boolean;

  constructor(private service: Service) { }

  public ngOnInit() {

    this.form = new FormGroup({});
    this.fields = this.getFields();
    this.model = {};

    this.insertModeEnabled = false;
  }

  public onPreviousClicked() {
    this.previousViewEvent.emit();
  }

  public onNextClicked() {
    if (this.insertModeEnabled) {
      this.service.toast("Speichern Sie zuerst Ihre Eingaben um fortzufahren.", "warning");
      return;
    }

    this.nextViewEvent.emit(this.installationData);
  }

  public getFields(): FormlyFieldConfig[] {

    let fields: FormlyFieldConfig[] = [];

    fields.push({
      key: "alias",
      type: "input",
      templateOptions: {
        label: "Bezeichnung",
        description: "wird im Online-Monitoring angezeigt, z. B. ''Garage''",
        required: true
      }
    });

    fields.push({
      key: "value",
      type: "input",
      templateOptions: {
        type: "number",
        label: "Installierte Leistung [Wₚ]",
        min: 1000,
        required: true
      },
      parsers: [Number]
    });

    fields.push({
      key: "orientation",
      type: "select",
      templateOptions: {
        label: "Ausrichtung",
        options: [
          { label: "", value: undefined },
          { label: "Nord", value: "n" },
          { label: "Nordost", value: "no" },
          { label: "Ost", value: "o" },
          { label: "Südost", value: "so" },
          { label: "Süd", value: "s" },
          { label: "Südwest", value: "sw" },
          { label: "West", value: "w" },
          { label: "Nordwest", value: "nw" },
        ]
      }
    });

    fields.push({
      key: "moduleType",
      type: "input",
      templateOptions: {
        label: "Modultyp",
        description: "z. B. Hersteller und Leistung"
      }
    });

    fields.push({
      key: "modulesPerString",
      type: "input",
      templateOptions: {
        type: "number",
        label: "Anzahl Module"
      }
    });

    fields.push({
      key: "meterType",
      type: "select",
      templateOptions: {
        label: "Zählertyp",
        options: [
          { label: "SOCOMEC", value: "socomec" }
        ]
      },
      defaultValue: "socomec"
    });

    fields.push({
      key: "modbusCommunicationAddress",
      type: "input",
      templateOptions: {
        type: "number",
        label: "Modbus Kommunikationsadresse",
        description: "Der Zähler muss mit den folgenden Parametern konfiguriert werden: Kommunikationsgeschwindigkeit (bAud) ''9600'', Kommunikationsparität (PrtY) ''n'', Kommunikations-Stopbit (StoP) ''1''"
      },
      defaultValue: 6
    });

    return fields;

  }

  public switchMode() {

    if (this.insertModeEnabled) {

      // Test if form is valid
      if (this.form.invalid) {
        this.service.toast("Geben Sie gültige Daten ein um zu Speichern.", "danger");
        return;
      }

      // Push data into array and reset the form
      this.installationData.pv.ac.push(Utils.deepCopy(this.model));
      this.form.reset();

    }

    // Switch
    this.insertModeEnabled = !this.insertModeEnabled;
  }

  public editElement(element) {
    this.model = element;

    if (!this.insertModeEnabled) {
      this.switchMode();
    }

    this.removeElement(element);
  }

  public removeElement(element) {
    let ac = this.installationData.pv.ac;
    ac.splice(ac.indexOf(element), 1);
  }

}