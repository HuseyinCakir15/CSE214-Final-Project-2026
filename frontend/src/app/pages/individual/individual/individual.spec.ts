import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Individual } from './individual';

describe('Individual', () => {
  let component: Individual;
  let fixture: ComponentFixture<Individual>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Individual],
    }).compileComponents();

    fixture = TestBed.createComponent(Individual);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
