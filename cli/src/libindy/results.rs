use super::ErrorCode;
use utils::timeout::TimeoutUtils;

use std::sync::mpsc::Receiver;

pub fn result_to_empty(err: ErrorCode, receiver: Receiver<ErrorCode>) -> Result<(), ErrorCode> {
    if err != ErrorCode::Success {
        return Err(err);
    }

    let err = receiver.recv_timeout(TimeoutUtils::short_timeout()).unwrap();

    if err != ErrorCode::Success {
        return Err(err);
    }

    Ok(())
}

pub fn result_to_int(err: ErrorCode, receiver: Receiver<(ErrorCode, i32)>) -> Result<i32, ErrorCode> {
    if err != ErrorCode::Success {
        return Err(err);
    }

    let (err, val) = receiver.recv_timeout(TimeoutUtils::medium_timeout()).unwrap();

    if err != ErrorCode::Success {
        return Err(err);
    }

    Ok(val)
}

pub fn result_to_string(err: ErrorCode, receiver: Receiver<(ErrorCode, String)>) -> Result<String, ErrorCode> {
    if err != ErrorCode::Success {
        return Err(err);
    }

    let (err, val) = receiver.recv_timeout(TimeoutUtils::medium_timeout()).unwrap();

    if err != ErrorCode::Success {
        return Err(err);
    }

    Ok(val)
}

pub fn result_to_string_string(err: ErrorCode, receiver: Receiver<(ErrorCode, String, String)>) -> Result<(String, String), ErrorCode> {
    if err != ErrorCode::Success {
        return Err(err);
    }

    let (err, val, val2) = receiver.recv_timeout(TimeoutUtils::medium_timeout()).unwrap();

    if err != ErrorCode::Success {
        return Err(err);
    }

    Ok((val, val2))
}