import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatError } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';
import { HostService } from 'src/app/hosts/host.service';
import { Team } from '../team';

@Component({
  selector: 'app-select-team',
  templateUrl: './select-team.component.html',
  styleUrls: ['./select-team.component.scss'],
  standalone: true,
  imports: [MatError, CommonModule]
})
export class SelectTeamComponent implements OnInit {
  teams: Team[];
  startedHuntId: string;
  accessCode: string;

  constructor(private route: ActivatedRoute, private http: HttpClient, private router: Router, private hostService: HostService) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accessCode = params['accessCode'];
      console.log('Access Code:', this.accessCode);
      this.hostService.getStartedHunt(this.accessCode).subscribe(startedHunt => {
        // Set startedHuntId to startedHunt._id instead of accessCode
        this.startedHuntId = startedHunt._id;
        console.log('startedHuntId:', this.startedHuntId);
        this.hostService.getTeams(this.startedHuntId).subscribe(teams => {
          // Initialize selected property for each team
          this.teams = teams.map(team => ({ ...team, selected: false }));
        });
      });
    });
  }

  toggleSelection(team: Team): void {
    // Deselect all other teams
    this.teams.forEach(t => {
      if (t !== team) {
        t.selected = false;
      }
    });

    // Toggle the selection state of the clicked team
    team.selected = !team.selected;
  }

  isTeamSelected(): boolean {
    // Check if at least one team is selected
    return this.teams && this.teams.some(team => team.selected);
  }

  proceed(): void {
    // Find the ID of the selected team
    const selectedTeamId = this.teams.find(team => team.selected)._id;
    // Navigate to the next page with the selected team ID
    this.router.navigate(['/hunter-view', this.accessCode, 'teams', selectedTeamId]);
  }

  // Function to track items by their ID
  trackById(index: number, item: Team): string {
    return item._id;
  }
}
