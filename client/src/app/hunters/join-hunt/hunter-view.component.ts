import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subject, forkJoin } from 'rxjs';
import { map, switchMap, takeUntil } from 'rxjs/operators';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Task } from 'src/app/hunts/task';
import { HuntCardComponent } from 'src/app/hunts/hunt-card.component';
import { HostService } from 'src/app/hosts/host.service';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { Team } from './team';
import { Submission } from './Submission';


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
  team: Team;
  teamId: string;

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
      map((params: ParamMap) => {
        return {
          accessCode: params.get('accessCode'),
          teamId: params.get('teamId')
        };
      }),
      switchMap(({accessCode, teamId}) => {
        this.teamId = teamId;
        return forkJoin({
          startedHunt: this.hostService.getStartedHunt(accessCode),
          submissions: this.hostService.getTeamSubmissions(teamId)
        });
      }),
      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: ({startedHunt, submissions}) => {
        this.startedHunt = startedHunt;
        this.loadPhotos(submissions);
      },
      error: (_err) => {
        this.error = {
          help: 'There is an error trying to load the tasks or the submissions - Please try to run the hunt again',
          httpResponse: _err.message,
          message: _err.error?.title,
        };
      },
    });
  }

  loadPhotos(submissions: Submission[]): void {
    if (submissions.length === 0) {
      console.log('No submissions to process.');
      return;
    }

    for (const submission of submissions) {
      const task = this.startedHunt.completeHunt.tasks.find(task => task._id === submission.taskId);
      if (task) {
        task.status = true;
        task.photos.push(submission._id);
        this.hostService.getPhoto(submission._id).subscribe({
          next: (photoBase64: string) => {
            console.log(photoBase64);
            this.imageUrls[task._id] = this.hostService.convertToImageSrc(photoBase64);
          },
          error: (_err) => {
            console.error('Error loading photo', _err);
          },
        });
      }
    }
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
        if (task.photos.length > 0) {
          this.replacePhoto(file, task, this.teamId);
        }
        else {
          this.submitPhoto(file, task, this.teamId);
        }
      }
    }
  }

  submitPhoto(file: File, task: Task, teamId: string): void {
    this.hostService.submitPhoto(this.startedHunt._id, task._id, file, teamId).subscribe({
      next: (photoId: string) => {
        task.status = true;
        task.photos.push(photoId);
        this.snackBar.open('Photo uploaded successfully', 'Close', {
          duration: 3000
        });
      },
      error: (error: Error) => {
        console.error('Error uploading photo', error);
        this.snackBar.open('Error uploading photo. Please try again', 'Close', {
          duration: 3000
        });
      },
    });
  }

replacePhoto(file: File, task: Task, teamId: string): void {
  this.hostService.replacePhoto(this.startedHunt._id, task._id, task.photos[0], file, teamId).subscribe({
    next: (photoId: string) => {
      task.photos[0] = photoId;
      this.snackBar.open('Photo replaced successfully', 'Close', {
        duration: 3000
      });
    },
    error: (error: Error) => {
      console.error('Error replacing photo', error);
      this.snackBar.open('Error replacing photo. Please try again', 'Close', {
        duration: 3000
      });
    },
  });
}

}
