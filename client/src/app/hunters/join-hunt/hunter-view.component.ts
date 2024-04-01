import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subject, of } from 'rxjs';
import { catchError, map, switchMap, takeUntil } from 'rxjs/operators';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Task } from 'src/app/hunts/task';
import { HuntCardComponent } from 'src/app/hunts/hunt-card.component';
import { HostService } from 'src/app/hosts/host.service';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';


@Component({
  selector: 'app-hunter-view',
  standalone: true,
  imports: [HuntCardComponent, CommonModule, MatCardModule, MatIconModule],
  templateUrl: './hunter-view.component.html',
  styleUrl: './hunter-view.component.scss'
})
export class HunterViewComponent implements OnInit, OnDestroy {
  startedHunt: StartedHunt;
  tasks: Task[] = [];
  error: { help: string, httpResponse: string, message: string };
  imageUrls = {};

  private ngUnsubscribe = new Subject<void>();

  constructor(
    private hostService: HostService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private router: Router,
    public dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      map((params: ParamMap) => params.get('accessCode')),
      switchMap((accessCode: string) => this.hostService.getStartedHunt(accessCode)),

      takeUntil(this.ngUnsubscribe)
      ).subscribe({
        next: startedHunt => {
          this.startedHunt = startedHunt;
          return;
        },
        error: _err => {
          this.error = {
            help: 'There is an error trying to load the tasks - Please try to run the hunt again',
            httpResponse: _err.message,
            message: _err.error?.title,
          };
        }
      });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  onFileSelected(event, task: Task): void {
    const file: File = event.target.files[0];
    const fileType = file.type;
    if (fileType.match(/image\/*/)) {
      if (this.imageUrls[task._id] && !window.confirm('An image has already been uploaded for this task. Are you sure you want to replace it?')) {
        return;
      }

      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = (event: ProgressEvent<FileReader>) => {
        this.imageUrls[task._id] = event.target.result.toString();
      };

      if (file) {
        this.hostService.submitPhoto(task._id, file).pipe(
          catchError((error: Error) => {
            console.error('Error uploading photo', error);
            this.snackBar.open('Error uploading photo. Please try again', 'Close', {
              duration: 3000
            });
            return of(null);
          })
        ).subscribe(() => {
          task.status = true;
          this.snackBar.open('Photo uploaded successfully', 'Close', {
            duration: 3000
          });
        });
      }
    }
  }

}
