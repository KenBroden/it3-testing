import { Component, OnDestroy, OnInit } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ActivatedRoute, Router } from "@angular/router";
import { HostService } from "../hosts/host.service";
import { MatSelect } from "@angular/material/select";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { StartedHunt } from "./startedHunt";


@Component({
  selector: 'app-add-teams',
  templateUrl: './add-teams.component.html',
 // styleUrls: ['./add-teams.component.css']
  standalone: true,
  imports: [MatSelect, FormsModule, CommonModule]
})
export class AddTeamsComponent implements OnInit, OnDestroy{
  numTeams: number = 1;
  startedHunt: StartedHunt;
  accessCode: string;
  startedHuntId: string;

  constructor(private snackBar: MatSnackBar, private route: ActivatedRoute, private hostService: HostService, private router: Router) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accessCode = params['accessCode'];
      this.hostService.getStartedHunt(this.accessCode).subscribe(startedHunt => {
        this.startedHunt = startedHunt;
      });
    });
  }

  ngOnDestroy(): void {
    this.snackBar.dismiss();
  }

  addTeams(id: string, numTeams: number): void {
    if (this.startedHunt) {
      this.hostService.addTeams(id ,numTeams).subscribe(() => {
        this.router.navigate(['/startedHunts/', this.startedHunt.accessCode]);
      });
    } else {
      console.error('startedHunt is undefined');
    }
  }
}
