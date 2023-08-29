import {Component, DoCheck, IterableDiffers, OnInit, ViewChild, ViewChildren} from '@angular/core';
import {Sector, SectorType} from '../../dtos/sector';
import {CdkDragDrop, CdkDragMove, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {ToastrService} from 'ngx-toastr';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {SectorComponent} from './sector/sector.component';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {AuthService} from '../../services/auth.service';
import {SeatmapService} from '../../services/seatmap.service';
import {Seatmap} from '../../dtos/seatmap';
import {Title} from '@angular/platform-browser';
import {MatSliderModule} from '@angular/material/slider';

export enum SeatmapCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-seatmap',
  templateUrl: './seatmap.component.html',
  styleUrls: ['./seatmap.component.scss'],
})
export class SeatmapComponent implements OnInit, DoCheck {

  @ViewChildren('secId') sectorComponents: SectorComponent[];

  @ViewChild('secToModify') sectorToModifyComponent: SectorComponent;

  seatmapInstance: Seatmap;

  seatmap: Sector[][];
  toBeDragged: Sector[];
  selectedSectorType = null;
  maxScaleFactor = 1;
  isStanding = false;
  mode: SeatmapCreateEditMode;
  inputFormControl = new FormControl('', [Validators.required]);
  sectorForm = new FormGroup({
    sectorType: new FormControl('', Validators.required),
    sectorRows: new FormControl('', Validators.required),
    sectorColumns: new FormControl('', Validators.required),
    lodgeSize: new FormControl(''),
    sectorPrice: new FormControl('', Validators.required),
    sectorName: new FormControl('', Validators.required),
    isStanding: new FormControl('false', Validators.required)
  });
  seatmapForm = new FormGroup({
    seatmapName: new FormControl('', Validators.required)
  });

  sectorToBeModifiedCopy: Sector = new Sector();
  sectorToBeModified: Sector = new Sector();

  iterableDiffer: any;


  // eslint-disable-next-line @typescript-eslint/naming-convention
  protected readonly SectorType = SectorType;


  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private location: Location,
    private authService: AuthService,
    private service: SeatmapService,
    private iterableDiffers: IterableDiffers,
    private titleService: Title) {
    this.titleService.setTitle('Seatmap - Ticketline');
    this.iterableDiffer = iterableDiffers.find([]).create(null);
  }

  get modeIsCreate(): boolean {
    return this.mode === SeatmapCreateEditMode.create;
  }

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.notification.error('You must be logged in as admin to create/edit seatmaps.', 'Not Logged In');
      this.router.navigate(['/login']);
    } else if (this.authService.getUserRole() !== 'ADMIN') {
      this.notification.error('You must be an admin to create/edit seatmaps.', 'Not Admin');
      this.router.navigate(['/home']);
    }

    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
    if (this.mode === SeatmapCreateEditMode.edit) {
      this.route.params.subscribe(params => {
        this.service.getSeatmapById(params.id).subscribe({
          next: data => {
            this.seatmapInstance = data;
            this.seatmap = data.sectors;
            this.populateEditSeatmap();
            this.seatmapForm.get('seatmapName').setValue(data.name);
            console.log(this.seatmap);
            this.applySectorScaleTransform(200, 'sector');
          },
          error: err => {
            console.error('Error getting seatmap', err);
            const errorMessage = err.status === 0
              ? 'Is the backend up?'
              : err.message.message;
            const toDisplay = err.error;
            this.notification.error(errorMessage, 'Could Not Get Seatmap: ' + toDisplay);
            this.router.navigate(['']);
          }
        });
      });
    } else {
      this.seatmapInstance = new Seatmap();
      this.seatmap = [];
      for (let i = 0; i < 6; i++) {
        this.seatmap[i] = [];
        for (let j = 0; j < 6; j++) {
          this.seatmap[i][j] = null;
        }
      }
      this.seatmapInstance = new Seatmap();
    }
    this.toBeDragged = [];
    // const scaleSlider = document.getElementById('scaleSlider') as HTMLInputElement;
    // scaleSlider.addEventListener('input', () => {
    //   console.log('scaleSlider.value: ' + scaleSlider.value);
    //   this.applySectorScaleTransform(+scaleSlider.value, 'modifySector');
    // });
  }

  ngDoCheck(): void {
    const changes = this.iterableDiffer.diff(this.sectorComponents);
    if (changes) {
      this.updateFunctions();
    }
  }

  updateFunctions = () => {

  };

  nameSeatmap() {
    const nameOfSeatmap = this.seatmapForm.value.seatmapName;
    if (nameOfSeatmap !== undefined && nameOfSeatmap !== null && nameOfSeatmap !== '') {
      this.seatmapInstance.name = nameOfSeatmap;
      this.save();
    } else {
      this.notification.error('Please name your seatmap', 'No Name');
    }
  }

  //fill the seatmap with empty sectors for the edit mode
  populateEditSeatmap() {
    let sectorRowLength = 6;
    const seatmapLength = this.seatmap.length > 6 ? this.seatmap.length : 6;
    //check if any row contains more sectors than the default 6
    for (const item of this.seatmap) {
      if (item.length > sectorRowLength) {
        sectorRowLength = item.length;
      }
    }
    for (let i = 0; i < seatmapLength; i++) {
      if (this.seatmap[i] === undefined || this.seatmap[i] === null) {
        this.seatmap[i] = [];
      }
      for (let j = 0; j < sectorRowLength; j++) {
        if (this.seatmap[i][j] === undefined || this.seatmap[i][j] === null) {
          this.seatmap[i][j] = null;
        }
      }
    }
  }

  //if sectorElement is bigger than maxSize in height or width, scale it down to maxSize
  applySectorScaleTransform(maxSize: number, queryName: string) {
    setTimeout(() => {
      const query = '[id^=' + queryName + '-]';
      const sectorElements = document.querySelectorAll(query);
      // @ts-ignore
      Array.from(sectorElements).forEach((sector: HTMLElement) => {
        console.log(sector);
        const nodeList = Array.from(sector.childNodes);
        const containsSectorNode = nodeList.some(node => node.nodeName === 'APP-SECTOR');
        const currentHeight = sector.offsetHeight;
        const currentWidth = sector.offsetWidth;
        //either if sector is in seatmap and has a childNode sector or if sector is in drag zone
        if (containsSectorNode || sector.nodeName === 'APP-SECTOR') {
          if (currentHeight > maxSize || currentWidth > maxSize && queryName === 'sector') {
            const heightFactor = maxSize / currentHeight;
            const widthFactor = maxSize / currentWidth;
            let scaleFactor = Math.min(heightFactor, widthFactor);
            //if sector is not to be modified and scaleFactor is smaller than maxScaleFactor, set maxScaleFactor to scaleFactor
            if (scaleFactor < this.maxScaleFactor && queryName === 'sector') {
              this.maxScaleFactor = scaleFactor;
            } else if (queryName === 'sector') { //sector not to be modified, but scaleFactor is bigger than maxScaleFactor
              scaleFactor = this.maxScaleFactor;
            }
            //scale down sector
            console.log('scale down sector' + scaleFactor);
            sector.style.transform = `scale(${scaleFactor})`;
            //get new height and width
            const tranformedSector = sector.getBoundingClientRect();
            const transformedHeight = tranformedSector.height;
            const transformedWidth = tranformedSector.width;
            //set margins to align/justify sector top left
            sector.style.marginLeft = `-${(currentWidth - transformedWidth) / 2}px`;
            sector.style.marginRight = `-${(currentWidth - transformedWidth) / 2}px`;
            sector.style.marginTop = `-${(currentHeight - transformedHeight) / 2}px`;
            sector.style.marginBottom = `-${(currentHeight - transformedHeight) / 2}px`;
          } else if (queryName === 'sector') {
            //scale smaller sectors down with maxScaleFactor
            console.log('scale smaller sectors down with maxScaleFactor');
            sector.style.transform = `scale(${this.maxScaleFactor})`;
            //get new height and width
            const tranformedSector = sector.getBoundingClientRect();
            const transformedHeight = tranformedSector.height;
            const transformedWidth = tranformedSector.width;
            //set margins to align/justify sector top left
            sector.style.marginLeft = `-${(currentWidth - transformedWidth) / 2}px`;
            sector.style.marginRight = `-${(currentWidth - transformedWidth) / 2}px`;
            sector.style.marginTop = `-${(currentHeight - transformedHeight) / 2}px`;
            sector.style.marginBottom = `-${(currentHeight - transformedHeight) / 2}px`;
          } else if (queryName === 'modifySector'){
            //modifySector
            this.applyModifyScaleTransform(maxSize, sector);
          }
        } else {//delete styles if elements do not contain sector
          sector.style.transform = null;
          sector.style.margin = null;
        }
      });
    }, 3);
  }
  applyModifyScaleTransform(maxSize: number, sector: HTMLElement) {
    //modifySector
    console.log('modifySector');
    console.log(sector);
    console.log(maxSize);
    const currentHeight = sector.offsetHeight;
    const currentWidth = sector.offsetWidth;
    //scale sector
    if (currentHeight >= maxSize || currentWidth >= maxSize) {
      console.log('scale modifySector');
      const heightFactor = maxSize / currentHeight;
      const widthFactor = maxSize / currentWidth;
      const scaleFactor = Math.min(heightFactor, widthFactor);
      sector.style.transform = `scale(${scaleFactor})`;
      //get new height and width
      const tranformedSector = sector.getBoundingClientRect();
      const transformedHeight = tranformedSector.height;
      const transformedWidth = tranformedSector.width;
      //set margins to align/justify sector top left
      sector.style.marginLeft = `-${(currentWidth - transformedWidth) / 2}px`;
      sector.style.marginRight = `-${(currentWidth - transformedWidth) / 2}px`;
      sector.style.marginTop = `-${(currentHeight - transformedHeight) / 2}px`;
      sector.style.marginBottom = `-${(currentHeight - transformedHeight) / 2}px`;
    }else{
      //sector is smaller than maxSize, just show how it is
      console.log('no modification');
      sector.style.transform = null;
      sector.style.margin = null;
    }
  }

  save() {
    this.enumerateSectors();
    this.seatmapInstance.sectors = this.seatmap;
    if (this.modeIsCreate) {
      this.service.createSeatmap(this.seatmapInstance).subscribe({
        next: data => {
          this.notification.success('Seatmap created successfully', 'Success');
          this.router.navigate(['/seatmap']);
        },
        error: err => {
          console.error('Error creating seatmap', err);
          const errorMessage = err.status === 0
            ? 'Is the backend up?'
            : err.message.message;
          this.notification.error(errorMessage, 'Could Not Create Seatmap: ' + this.seatmapInstance.name);
        }
      });
    } else {
      this.service.updateSeatmap(this.seatmapInstance).subscribe({
        next: data => {
          this.notification.success('Seatmap updated successfully', 'Success');
          this.router.navigate(['/seatmap']);
        },
        error: err => {
          console.error('Error updating seatmap', err);
          const errorMessage = err.status === 0
            ? 'Is the backend up?'
            : err.message.message;
          const toDisplay = err.error;
          this.notification.error(errorMessage, 'Could Not Update Seatmap: ' + toDisplay);
        }
      });
    }
  }

  enumerateSectors(): void {
    for (let i = 0; i < this.seatmap.length; i++) {
      for (let j = 0; j < this.seatmap[i].length; j++) {
        if (this.seatmap[i][j]) {
          this.seatmap[i][j].row = i;
          this.seatmap[i][j].column = j;
        }
      }
    }
  }

  addSectorToBeDragged(sectors: Sector[]) {
    if (this.sectorForm.valid) {
      if (this.hasNameDuplicate(this.sectorForm.value.sectorName) === true) {
        return;
      }
      const sector = new Sector();
      if (this.sectorForm.value.lodgeSize) {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        sector.name = this.sectorForm.value.sectorName;
        sector.type = this.transformSectorType(this.selectedSectorType);
        sector.length = +this.sectorForm.value.sectorColumns;
        sector.width = +this.sectorForm.value.sectorRows;
        sector.lodgeSize = +this.sectorForm.value.lodgeSize;
        sector.price = +this.sectorForm.value.sectorPrice;
        sector.standingSector = JSON.parse(this.sectorForm.value.isStanding);
        sectors.push(sector);
        this.applySectorScaleTransform(200, 'sector');
      } else {
        sector.name = this.sectorForm.value.sectorName;
        sector.type = this.transformSectorType(this.selectedSectorType);
        sector.length = +this.sectorForm.value.sectorColumns;
        sector.width = +this.sectorForm.value.sectorRows;
        sector.price = +this.sectorForm.value.sectorPrice;
        sector.standingSector = JSON.parse(this.sectorForm.value.isStanding);
        sectors.push(sector);
        this.applySectorScaleTransform(200, 'sector');
      }
    } else {
      this.notification.error('Please fill in all required fields');
    }
  }

  hasNameDuplicate(name: string): boolean {
    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for (let i = 0; i < this.seatmap.length; i++) {
      // eslint-disable-next-line @typescript-eslint/prefer-for-of
      for (let j = 0; j < this.seatmap[i].length; j++) {
        if (this.seatmap[i][j] != null && this.seatmap[i][j].name === name) {
          this.notification.error('Sector with this name already exists', 'Name duplicate');
          return true;
        }
      }
    }
    for (const dragSector of this.toBeDragged) {
      if (dragSector != null && dragSector.name === name) {
        this.notification.error('Sector with this name already exists', 'Name duplicate');
        return true;
      }
    }
    return false;
  }

  drop(event: CdkDragDrop<Sector[]>) {
    //dropped in same container
    if (event.previousContainer === event.container) {
      // eslint-disable-next-line max-len
      ///if the item is dropped on an empty space
      if (event.container.data[event.currentIndex] === null
        || event.container.data[event.currentIndex] === undefined) {
        event.container.data[event.currentIndex] = event.container.data[event.previousIndex];
        event.container.data[event.previousIndex] = null;
        this.applySectorScaleTransform(200, 'sector');
        console.log('drop on empty, same container');
      } else {//if the item is dropped on an occupied space
        moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
        console.log('moveItemInArray, same container');
        this.applySectorScaleTransform(200, 'sector');
      }
    } else {//if item is dropped in different container
      if (event.container.data[event.currentIndex] === null
        || event.container.data[event.currentIndex] === undefined) {///if the item is dropped on an empty space
        if (event.previousContainer.data[event.previousIndex] === null
          || event.previousContainer.data[event.previousIndex] === undefined) {///if empty item is dropped on an empty space
          this.notification.info('You can\'t drop an empty item on an empty space');
          return;
        } else {//if item is dropped on an empty space
          event.container.data[event.currentIndex] = event.previousContainer.data[event.previousIndex];
          event.previousContainer.data.splice(event.previousIndex, 1);
          this.fillArrayGaps(event.container.data.length);
          console.log('sector dropped on empty space in another container');
          this.applySectorScaleTransform(200, 'sector');
        }
      } else {//item is dropped on an occupied space
        console.log('item dropped on occupied space in another container');
        transferArrayItem(
          event.previousContainer.data,
          event.container.data,
          event.previousIndex,
          event.currentIndex,
        );
        this.fillArrayGaps(event.container.data.length);
        this.applySectorScaleTransform(200, 'sector');
      }
    }
  }

  fillArrayGaps(arrayLength: number) {
    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for (let i = 0; i < this.seatmap.length; i++) {
      for (let j = arrayLength - 2; j < arrayLength; j++) {
        if (this.seatmap[i][j] === undefined || this.seatmap[i][j] === null) {
          this.seatmap[i][j] = null;
        }
      }
    }
  }

  checkIfAddRowOrColumn(event: CdkDragMove<Sector>
  ) {
    const seatmapPosition = document.getElementById('customizableSeatmap').getBoundingClientRect();
    if (event.pointerPosition.x > seatmapPosition.right && this.seatmap[0].length < 10) {
      this.addColumn();
    } else if (event.pointerPosition.y > seatmapPosition.bottom && this.seatmap.length < 10) {
      this.addRow();
    }
  }

  addRow() {
    this.seatmap.push([]);
    for (let i = 0; i < this.seatmap[i].length; i++) {
      this.seatmap[this.seatmap.length - 1][i] = null;
    }
  }

  addColumn() {
    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for (let i = 0; i < this.seatmap.length; i++) {
      this.seatmap[i].push(null);
    }
  }


  removeSectorFromDragZone(sectors: Sector[]) {
    const toBeRemoved = sectors[sectors.length - 1];
    if (toBeRemoved === undefined || toBeRemoved === null) {
      return;
    }
    sectors.pop();
  }

  transformSectorType(sectorType: string
  ) {
    if (sectorType === 'regular') {
      return SectorType.regular;
    } else if (sectorType === 'grandstandCurveLeft') {
      return SectorType.grandstandCurveLeft;
    } else if (sectorType === 'grandstandCurveRight') {
      return SectorType.grandstandCurveRight;
    } else if (sectorType === 'lodge') {
      return SectorType.lodge;
    }
  }

  toggleSectorModification(sector: Sector, secId: SectorComponent
  ) {
    console.log('toggleSectorModification');
    if (sector.noUpdate) {
      return;
    }
    this.sectorToBeModifiedCopy = this.service.deepCopySector(sector);
    this.sectorToBeModified = sector;

    const sectorModification = document.getElementById('sectorModification');
    if (sectorModification.style.display === 'none') {
      sectorModification.style.display = 'flex';
    } else {
      sectorModification.style.display = 'none';
    }
    // Überprüfe, ob bereits ein h2-Element vorhanden ist
    const h2Element = sectorModification.querySelector('h2');
    if (h2Element) {
      sectorModification.removeChild(h2Element);
    }
    const h2 = document.createElement('h2');
    h2.textContent = 'selected sector: ' + sector.name;
    sectorModification.insertBefore(h2, sectorModification.firstChild);
    h2.style.width = 'fit-content';
    h2.style.height = 'fit-content';
    h2.style.fontSize = '1.5rem';
    this.addSectorFunctions(secId);
    this.applySectorScaleTransform(260, 'modifySector');
  }

  rotate = () => {
  };
  increaseLeft = () => {
  };
  increaseRight = () => {
  };
  increaseTop = () => {
  };
  increaseBottom = () => {
  };
  decreaseLeft = () => {
  };
  decreaseRight = () => {
  };
  decreaseTop = () => {
  };
  decreaseBottom = () => {
  };
  saveDeletions = () => {
  };
  removeSectorFromSeatmap = () => {
  };

  addSectorFunctions(secId: SectorComponent
  ) {
    const sector: SectorComponent = this.sectorComponents.find(
      (sectorComponent) => sectorComponent === secId
    );

    this.saveDeletions = () => {
      this.sectorToModifyComponent.saveDeletions();
      this.applySectorScaleTransform(200, 'sector');
    };

    this.rotate = () => {
      sector.rotateSector();
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.increaseLeft = () => {
      sector.changeSectorLeft(1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.increaseRight = () => {
      sector.changeSectorRight(1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.increaseTop = () => {
      sector.changeSectorTop(1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.increaseBottom = () => {
      sector.changeSectorBottom(1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.decreaseLeft = () => {
      sector.changeSectorLeft(-1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.decreaseRight = () => {
      sector.changeSectorRight(-1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.decreaseTop = () => {
      sector.changeSectorTop(-1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };

    this.decreaseBottom = () => {
      sector.changeSectorBottom(-1);
      this.resetSectorModification();
      this.applySectorScaleTransform(200, 'sector');
      this.applySectorScaleTransform(260, 'modifySector');
    };
    this.removeSectorFromSeatmap = () => {
      for (const sectorRow of this.seatmap) {
        if (sectorRow == null) {
          continue;
        }
        for (let i = 0; i < sectorRow.length; i++) {
          if (sectorRow[i] !== null && sectorRow[i].name === sector.sector.name) {
            sectorRow[i] = null;
            const sectorModification = document.getElementById('sectorModification');
            if (sectorModification.style.display === 'flex') {
              sectorModification.style.display = 'none';
            }
            return;
          }
        }
      }
    };

  }

  adaptSectorInSeatmap(sector: Sector) {
    this.updateFunctions = () => {
      const s: SectorComponent = this.sectorComponents.find(
        (sectorComponent) => sectorComponent.sector === this.sectorToBeModified
      );
      this.addSectorFunctions(s);
      this.updateFunctions = () => {

      };
    };
    this.seatmap.forEach((row, rowIndex) => {
      row.forEach((s, colIndex) => {
        if (s === this.sectorToBeModified) {
          this.seatmap[rowIndex][colIndex] = this.sectorToBeModifiedCopy;
        }
      });
    });
    this.sectorToBeModified = this.sectorToBeModifiedCopy;
    this.resetSectorModification();
  }

  resetSectorModification() {
    this.sectorToBeModifiedCopy = this.service.deepCopySector(this.sectorToBeModified);
  }


}
