// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

//import edu.wpi.first.math.VecBuilder;
//import edu.wpi.first.math.controller.PIDController;
//import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.estimator.DifferentialDrivePoseEstimator;
//import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
//import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
//import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
//import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DrivetrainConstants;
//import com.kauailabs.navx.frc.AHRS;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
//import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
public class Drivetrain extends SubsystemBase {
  private static Drivetrain instance;

  // Motores de tração
  private WPI_VictorSPX motorLeftFront = new WPI_VictorSPX(DrivetrainConstants.kMotorLeftFront);
  private WPI_VictorSPX motorLeftFollower = new WPI_VictorSPX(DrivetrainConstants.kMotorLeftRear);
  private WPI_VictorSPX motorRightFront = new WPI_VictorSPX(DrivetrainConstants.kMotorRightFront);
  private WPI_VictorSPX motorRightFollower = new WPI_VictorSPX(DrivetrainConstants.kMotorRightRear);
  private DifferentialDrive m_diffDrive;

  // Placa de navegação
  //private AHRS m_gyro;

  // Odometry class for tracking robot pose
  private DifferentialDriveKinematics kinematics;
  private Field2d field = new Field2d();

  private DifferentialDrivePoseEstimator m_poseEstimator;

  public Drivetrain(){
     /* factory default values */
        motorRightFront.configFactoryDefault();
        motorRightFollower.configFactoryDefault();
        motorLeftFront.configFactoryDefault();
        motorRightFollower.configFactoryDefault();

        motorLeftFollower.setNeutralMode(NeutralMode.Brake);
        motorLeftFront.setNeutralMode(NeutralMode.Brake);
        motorRightFollower.setNeutralMode(NeutralMode.Brake);
        motorRightFront.setNeutralMode(NeutralMode.Brake);
        /* set up followers */
        motorRightFollower.follow(motorRightFront);
        motorLeftFollower.follow(motorLeftFront);

        /* [3] flip values so robot moves forward when stick-forward/LEDs-green */
        motorRightFront.setInverted(true); // !< Update this
        motorLeftFront.setInverted(false); // !< Update this

        /*
         * set the invert of the followers to match their respective master controllers
         */
        motorRightFollower.setInverted(InvertType.FollowMaster);
        motorLeftFollower.setInverted(InvertType.FollowMaster);
        m_diffDrive = new DifferentialDrive(motorLeftFront, motorRightFront);
  }

  public static Drivetrain getInstance() {
    if (instance == null) {
      instance = new Drivetrain();
    }
    return instance;
  }
  /**
   * Sets the desired wheel speeds.
   *
   * @param speeds The desired wheel speeds.
   */
  public void setSpeeds(DifferentialDriveWheelSpeeds speeds) {
   // final double leftFeedforward = m_feedforward.calculate(speeds.leftMetersPerSecond);
   // final double rightFeedforward = m_feedforward.calculate(speeds.rightMetersPerSecond);
   // motorLeftFront.setVoltage(leftOutput + leftFeedforward);
   // motorRightFront.setVoltage(rightOutput + rightFeedforward);
  }

  /**
   * Drives the robot with the given linear velocity and angular velocity.
   *
   * @param xSpeed Linear velocity in m/s.
   * @param rot Angular velocity in rad/s.
   */
  public void drive(double xSpeed, double rot) {
    var wheelSpeeds = kinematics.toWheelSpeeds(new ChassisSpeeds(xSpeed, 0.0, rot));
    setSpeeds(wheelSpeeds);
  }
  public void updatePose() {
    //m_odometry.update(m_gyro.getRotation2d());
    field.setRobotPose(m_poseEstimator.getEstimatedPosition());

  }

  public void resetPose(Pose2d newPose) {
    
    //m_poseEstimator.resetPosition(m_gyro.getRotation2d(), leftEncoder1.getPosition(), rightEncoder1.getPosition(),
    //    newPose);
  }

  // Periodico só atualiza os dados no Dashboard para informações
  @Override
  public void periodic() {
   // SmartDashboard.putNumber("Giro", getYaw());
   // SmartDashboard.putNumber("Roll", getRoll());
   // SmartDashboard.putNumber("Angle", getPitch());
    SmartDashboard.putData("Train", m_diffDrive);

    //m_poseEstimator.update(m_gyro.getRotation2d(),
        //getLeftDistanceMeters(),
       // getRightDistanceMeters());
    //updatePose();
  }
  public void feed(){
    m_diffDrive.feed();
  }
  /*
   * Funcões de movimentação
   */

  // Função principal, movimenta o robo para frente e com curva
  public void setDriveMotors(double forward, double rotation) {
    final var xSpeed = forward;//m_speedLimiter.calculate(forward);

    final var rot = rotation;//m_rotLimiter.calculate(rotation);

    SmartDashboard.putNumber("Potencia Frente (%)", xSpeed * 40);
    SmartDashboard.putNumber("Potencia Curva (%)", rot * 40);

    /*
     * Volta positiva = Anti horario, esquerda vai para tras
     */
    double left = xSpeed - rot;
    double right = xSpeed + rot;
    tankDrive(left, right);

  }

  /**
   * Configura o drivetrain para limitar a velocidade
   * importante quando o braço está extendido
   */
  public void setMaxOutput(boolean set) {
    if (set)
      m_diffDrive.setMaxOutput(DrivetrainConstants.kMaxSpeedArmExtended);
    else
      m_diffDrive.setMaxOutput(1.0);
  }

  public void arcadeDrive(double forward, double rotation) {

    SmartDashboard.putNumber("Potencia Frente (%)", forward * 40.0);
    SmartDashboard.putNumber("Potencia Curva (%)", rotation * 40.0);
    m_diffDrive.arcadeDrive(forward, rotation);
    m_diffDrive.feed();

  }

  public void tankDriveVolts(double left, double right) {
    motorLeftFront.setVoltage(left);
    motorRightFront.setVoltage(right);
    m_diffDrive.feed();
  }

  public void tankDrive(double left, double right) {
    motorLeftFront.set(left);
    motorRightFront.set(right);
    m_diffDrive.feed();

  }

  public void stopDrivetrain() {
    tankDriveVolts(0, 0);
  }

  public double getLeftPower() {
    return (motorLeftFront.get() + motorLeftFollower.get()) / 2;
  }

  public double getRightPower() {
    return (motorRightFront.get() + motorRightFollower.get()) / 2;
  }

  /**
   * Funções de telemetria
   
  public Pose2d getPose() {
    return m_odometry.getPoseMeters();
  }*/

  // Captura o angulo que o robo está apontando
 /*  public double getYaw() {
    return m_gyro.getYaw();
  }

  // Zera a direção do robo
  public void resetYaw() {
    m_gyro.reset();
  }

  public double getRoll() {
    return m_gyro.getRoll();
  }

  public double getPitch() {
    return m_gyro.getPitch();
  }

  /**
   * Zeroes the heading of the robot
   
  public void zeroHeading() {
    m_gyro.reset();
  }

  /** Reset the gyro. 
  public void resetGyro() {
    m_gyro.reset();
  }*/

  /**
   * Resets the odometry to the specified pose
   * 
   * @param pose The pose to which to set the odometry
   */
  public void resetOdometry(Pose2d pose) {
   // zeroHeading();
   // m_odometry.resetPosition(m_gyro.getRotation2d(),
   //     getLeftDistanceMeters(), getRightDistanceMeters(),
       // pose);
  }

}
