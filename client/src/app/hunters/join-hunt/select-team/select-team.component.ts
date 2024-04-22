import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatError } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';

interface Team {
  _id: string;
  teamName: string;
  selected?: boolean; // Optional property to track selection
}

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

  constructor(private route: ActivatedRoute, private http: HttpClient, private router: Router) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.startedHuntId = params['startedHuntId'];
      this.http.get<Team[]>(`/api/startedHunts/${this.startedHuntId}/teams`).subscribe(teams => {
        // Initialize selected property for each team
        this.teams = teams.map(team => ({ ...team, selected: false }));
      });
    });
  }

  toggleSelection(team: Team): void {
    // Toggle the selection state of the team
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
    this.router.navigate(['/hunter-view', this.startedHuntId, 'teams', selectedTeamId]);
  }

  // Function to track items by their ID
  trackById(index: number, item: Team): string {
    return item._id;
  }
}
