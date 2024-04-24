import { HttpClientModule } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDivider } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { AddTaskComponent } from 'src/app/hunts/addTask/add-task.component';
import { EndedHuntCardComponent } from '../ended-hunt-card.component';
//import { EndedHunt } from '../endedHunt';
import { Subject, map, switchMap, takeUntil } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { HostService } from 'src/app/hosts/host.service';
import { CommonModule } from '@angular/common';
import { StartedHunt } from 'src/app/startHunt/startedHunt';
import { Submission } from 'src/app/hunters/join-hunt/Submission';
//import { Team } from 'src/app/hunters/join-hunt/team';

@Component({
  selector: 'app-ended-hunt-details',
  templateUrl: './ended-hunt-details.component.html',
  styleUrls: ['./ended-hunt-details.component.scss'],
  standalone: true,
  imports: [
    EndedHuntCardComponent,
    MatCardModule,
    AddTaskComponent,
    MatDivider,
    MatIconButton,
    MatIcon,
    HttpClientModule,
    CommonModule,
    RouterLink
  ],
})
export class EndedHuntDetailsComponent implements OnInit, OnDestroy {
  confirmDeleteHunt: boolean = false;
  startedHunt: StartedHunt;
  submissions: Submission[];
  error: { help: string; httpResponse: string; message: string };

  private ngUnsubscribe = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private hostService: HostService,
    private router: Router,
    public dialog: MatDialog
  ) {}

  taskSubmissions: { [taskId: string]: { teamName: string, photo: string }[] } = {};

  ngOnInit(): void {
    this.route.paramMap.pipe(
      map((paramMap: ParamMap) => paramMap.get('id')),
      switchMap((id: string) => this.hostService.getStartedHuntById(id)),
      takeUntil(this.ngUnsubscribe)
    ).subscribe({
      next: (startedHunt) => {
        this.startedHunt = startedHunt;
        if (this.startedHunt) {
          this.loadTaskSubmissions();
        } else {
          console.error('Started hunt is null');
        }
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  loadTaskSubmissions(): void {
    this.hostService.getAllStartedHuntTeams(this.startedHunt._id).subscribe({
      next: (teams) => {
        teams.forEach(team => {
          this.hostService.getTeamSubmissions(team._id).subscribe({
            next: (submissions) => {
              this.submissions = submissions; // Use the submissions array
              submissions.forEach(submission => {
                if (!this.taskSubmissions[submission.taskId]) {
                  this.taskSubmissions[submission.taskId] = [];
                }
                this.hostService.getPhoto(submission._id).subscribe(photo => {
                  this.taskSubmissions[submission.taskId].push({
                    teamName: team.teamName,
                    photo: this.hostService.convertToImageSrc(photo)
                  });
                });
              });
            },
            error: (err) => {
              console.error(err);
            }
          });
        });
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  getTeamName(teamId: string): void {
    this.hostService.getAllStartedHuntTeams(this.startedHunt._id).subscribe({
      next: (teams) => {
        const team = teams.find(team => team._id === teamId);
        if (team) {
          return team.teamName;
        } else {
          console.error(`Team with id ${teamId} not found.`);
        }
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  getTaskName(taskId: string): string {
    return this.startedHunt.completeHunt.tasks.find(
      (task) => task._id === taskId
    ).name;
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
